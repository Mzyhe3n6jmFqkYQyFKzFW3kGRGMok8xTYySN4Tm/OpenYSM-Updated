package com.elfmcys.yesstevemodel.model

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.data.DeepCopy
import com.elfmcys.yesstevemodel.data.IsEmpty
import com.elfmcys.yesstevemodel.data.NbtLoad
import com.elfmcys.yesstevemodel.data.NbtSave
import com.elfmcys.yesstevemodel.util.AbstractManager
import com.elfmcys.yesstevemodel.util.getCompoundOrNull
import com.elfmcys.yesstevemodel.util.getListOrNull
import com.elfmcys.yesstevemodel.util.getStringOrNull
import net.minecraft.nbt.*
import java.nio.file.Files
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull

data class PlayerServerData(
    var modelId: String? = null,
    var textureId: String? = null,
    val roamingStorage: MutableMap<String, MutableMap<String, Float>> = ConcurrentHashMap(),
    val authModels: MutableSet<String> = ConcurrentHashMap.newKeySet()
) : NbtSave, DeepCopy<PlayerServerData>, IsEmpty {

    override val isEmpty: Boolean
        get() = modelId.isNullOrEmpty() && textureId.isNullOrEmpty() && roamingStorage.isEmpty() && authModels.isEmpty()

    override fun deepCopy(): PlayerServerData {
        val copy = PlayerServerData(modelId, textureId)
        roamingStorage.forEach { (m, vars) ->
            copy.roamingStorage[m] = ConcurrentHashMap(vars)
        }
        copy.authModels.addAll(authModels)
        return copy
    }

    override fun save(): CompoundTag {
        val tag = CompoundTag()
        modelId?.let { tag.putString("model_id", it) }
        textureId?.let { tag.putString("texture_id", it) }
        if (roamingStorage.isNotEmpty()) {
            val roamingTag = CompoundTag()
            roamingStorage.forEach { (mId, vars) ->
                val varsTag = CompoundTag()
                vars.forEach { (k, v) -> varsTag.putFloat(k, v) }
                roamingTag.put(mId, varsTag)
            }
            tag.put("roaming_storage", roamingTag)
        }
        if (authModels.isNotEmpty()) {
            val authList = ListTag()
            authModels.forEach { authList.add(StringTag.valueOf(it)) }
            tag.put("auth_models", authList)
        }
        return tag
    }

    companion object : NbtLoad<PlayerServerData> {
        override fun load(tag: CompoundTag): PlayerServerData {
            val record = PlayerServerData()
            tag.getStringOrNull("model_id")?.let { record.modelId = it }
            tag.getStringOrNull("texture_id")?.let { record.textureId = it }
            tag.getCompoundOrNull("roaming_storage")?.let { roamingTag ->
                for (mId in roamingTag.keySet()) {
                    val varsTag = roamingTag.getCompoundOrEmpty(mId)
                    val varsMap = ConcurrentHashMap<String, Float>()
                    for (varName in varsTag.keySet()) {
                        varsMap[varName] = varsTag.getFloatOr(varName, 0.0f)
                    }
                    record.roamingStorage[mId] = varsMap
                }
            }
            tag.getListOrNull("auth_models")?.let { authList ->
                for (i in authList.indices) {
                    authList.getString(i).getOrNull()?.let { record.authModels.add(it) }
                }
            }
            return record
        }
    }
}

data class ServerSelectionData(
    val players: MutableMap<String, PlayerServerData> = ConcurrentHashMap()
) : NbtSave, DeepCopy<ServerSelectionData>, IsEmpty {

    override val isEmpty: Boolean
        get() = players.isEmpty()

    override fun deepCopy(): ServerSelectionData {
        val copy = ServerSelectionData()
        players.forEach { (uuid, player) ->
            copy.players[uuid] = player.deepCopy()
        }
        return copy
    }

    override fun save(): CompoundTag {
        val tag = CompoundTag()
        val playersTag = CompoundTag()
        players.forEach { (uuid, player) ->
            if (!player.isEmpty) {
                playersTag.put(uuid, player.save())
            }
        }
        tag.put("players", playersTag)
        return tag
    }

    companion object : NbtLoad<ServerSelectionData> {
        override fun load(tag: CompoundTag): ServerSelectionData {
            val result = ServerSelectionData()
            val playersTag = tag.getCompoundOrNull("players") ?: tag
            for (uuidStr in playersTag.keySet()) {
                val playerTag = playersTag.getCompoundOrEmpty(uuidStr)
                result.players[uuidStr] = PlayerServerData.load(playerTag)
            }
            return result
        }
    }
}

object ServerModelSelection : AbstractManager<ServerSelectionData>("server_selection.nbt", ServerSelectionData()) {

    override fun loadData(): ServerSelectionData =
        runCatching {
            if (Files.exists(dataPath)) {
                val tag = NbtIo.readCompressed(dataPath, NbtAccounter.unlimitedHeap())
                ServerSelectionData.load(tag)
            } else {
                ServerSelectionData()
            }
        }.getOrElse {
            Constants.LOGGER.error("Failed to load server model selection from NBT", it)
            ServerSelectionData()
        }

    override fun saveNow(saveData: ServerSelectionData) {
        runCatching {
            Files.createDirectories(dataPath.parent)
            NbtIo.writeCompressed(saveData.save(), dataPath)
        }.onFailure {
            Constants.LOGGER.error("Failed to save server model selection to NBT", it)
        }
    }

    @Synchronized
    fun savePlayerSelection(uuid: UUID, model: String?, texture: String?) {
        val record = data.players.computeIfAbsent(uuid.toString()) { PlayerServerData() }
        record.modelId = model
        record.textureId = texture
        save()
    }

    @Synchronized
    fun updateRoamingVars(uuid: UUID, model: String?, vars: Map<String, Float>) {
        if (model.isNullOrEmpty() || vars.isEmpty()) return
        val record = data.players.computeIfAbsent(uuid.toString()) { PlayerServerData() }
        val modelVars = record.roamingStorage.computeIfAbsent(model) { ConcurrentHashMap() }
        modelVars.putAll(vars)
        save()
    }

    @Synchronized
    fun getRoamingVars(uuid: UUID, model: String?): Map<String, Float> {
        if (model.isNullOrEmpty()) return emptyMap()
        val record = data.players[uuid.toString()] ?: return emptyMap()
        return record.roamingStorage[model]?.toMap() ?: emptyMap()
    }

    @Synchronized
    fun getPlayerModel(uuid: UUID): String? = data.players[uuid.toString()]?.modelId

    @Synchronized
    fun getPlayerTexture(uuid: UUID): String? = data.players[uuid.toString()]?.textureId

    @Synchronized
    fun hasSelection(uuid: UUID): Boolean {
        val record = data.players[uuid.toString()] ?: return false
        return record.modelId != null && record.textureId != null
    }

    @Synchronized
    fun getAuthModels(uuid: UUID): Set<String> =
        data.players[uuid.toString()]?.authModels?.toSet() ?: emptySet()

    @Synchronized
    fun hasAuthModel(uuid: UUID, modelId: String): Boolean =
        data.players[uuid.toString()]?.authModels?.contains(modelId) ?: false

    @Synchronized
    fun addAuthModel(uuid: UUID, modelId: String) {
        val record = data.players.computeIfAbsent(uuid.toString()) { PlayerServerData() }
        if (record.authModels.add(modelId)) save()
    }

    @Synchronized
    fun addAllAuthModels(uuid: UUID, modelIds: Collection<String>) {
        val record = data.players.computeIfAbsent(uuid.toString()) { PlayerServerData() }
        if (record.authModels.addAll(modelIds)) save()
    }

    @Synchronized
    fun removeAuthModel(uuid: UUID, modelId: String) {
        val record = data.players[uuid.toString()] ?: return
        if (record.authModels.remove(modelId)) save()
    }

    @Synchronized
    fun clearAuthModels(uuid: UUID) {
        val record = data.players[uuid.toString()] ?: return
        if (record.authModels.isNotEmpty()) {
            record.authModels.clear()
            save()
        }
    }

    @Synchronized
    fun clear() {
        data.players.clear()
        saveNow()
    }
}
