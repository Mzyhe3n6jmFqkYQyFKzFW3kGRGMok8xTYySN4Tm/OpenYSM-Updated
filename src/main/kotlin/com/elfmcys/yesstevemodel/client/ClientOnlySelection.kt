package com.elfmcys.yesstevemodel.client

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.data.DeepCopy
import com.elfmcys.yesstevemodel.data.IsEmpty
import com.elfmcys.yesstevemodel.data.NbtLoad
import com.elfmcys.yesstevemodel.data.NbtSave
import com.elfmcys.yesstevemodel.util.AbstractManager
import com.elfmcys.yesstevemodel.util.getCompoundOrNull
import com.elfmcys.yesstevemodel.util.getListOrNull
import com.elfmcys.yesstevemodel.util.getStringOrNull
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.nbt.*
import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull

data class ClientSelectionData(
    var modelId: String? = null,
    var textureId: String? = null,
    val roamingStorage: MutableMap<String, MutableMap<String, Float>> = ConcurrentHashMap(),
    val starModels: MutableSet<String> = ConcurrentHashMap.newKeySet()
) : NbtSave, DeepCopy<ClientSelectionData>, IsEmpty {
    override val isEmpty: Boolean
        get() = modelId.isNullOrEmpty() && textureId.isNullOrEmpty() && roamingStorage.isEmpty() && starModels.isEmpty()

    override fun deepCopy(): ClientSelectionData {
        val copy = ClientSelectionData(modelId, textureId)
        roamingStorage.forEach { (m, vars) ->
            copy.roamingStorage[m] = ConcurrentHashMap(vars)
        }
        copy.starModels.addAll(starModels)
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
        if (starModels.isNotEmpty()) {
            val starList = ListTag()
            starModels.forEach { starList.add(StringTag.valueOf(it)) }
            tag.put("star_models", starList)
        }
        return tag
    }

    companion object : NbtLoad<ClientSelectionData> {
        override fun load(tag: CompoundTag): ClientSelectionData {
            val data = ClientSelectionData()
            tag.getStringOrNull("model_id")?.let { data.modelId = it }
            tag.getStringOrNull("texture_id")?.let { data.textureId = it }
            tag.getCompoundOrNull("roaming_storage")?.let { roamingTag ->
                for (mId in roamingTag.keySet()) {
                    val varsTag = roamingTag.getCompoundOrEmpty(mId)
                    val varsMap = ConcurrentHashMap<String, Float>()
                    for (varName in varsTag.keySet()) {
                        varsMap[varName] = varsTag.getFloatOr(varName, 0.0f)
                    }
                    data.roamingStorage[mId] = varsMap
                }
            }
            tag.getListOrNull("star_models")?.let { starList ->
                for (i in starList.indices) {
                    starList.getString(i).getOrNull()?.let { data.starModels.add(it) }
                }
            }
            return data
        }
    }
}

@Environment(EnvType.CLIENT)
object ClientOnlySelection : AbstractManager<ClientSelectionData>("client_selection.dat", ClientSelectionData()) {
    override fun loadData(): ClientSelectionData =
        runCatching {
            if (Files.exists(dataPath)) {
                val tag = NbtIo.readCompressed(dataPath, NbtAccounter.unlimitedHeap())
                ClientSelectionData.load(tag)
            } else {
                ClientSelectionData()
            }
        }.getOrElse {
            Constants.LOGGER.error("Failed to load client-only model selection from NBT", it)
            ClientSelectionData()
        }

    override fun saveNow(saveData: ClientSelectionData) {
        runCatching {
            Files.createDirectories(dataPath.parent)
            NbtIo.writeCompressed(saveData.save(), dataPath)
        }.onFailure {
            Constants.LOGGER.error("Failed to save client-only model selection to NBT", it)
        }
    }

    @Synchronized
    fun save(model: String?, texture: String?) {
        data.modelId = model
        data.textureId = texture
        save()
    }

    @Synchronized
    fun updateRoamingVars(model: String?, vars: Map<String, Float>) {
        if (model.isNullOrEmpty() || vars.isEmpty()) return
        val map = data.roamingStorage.computeIfAbsent(model) { ConcurrentHashMap() }
        map.putAll(vars)
        save()
    }

    @Synchronized
    fun getRoamingVars(model: String?): Map<String, Float> {
        if (model.isNullOrEmpty()) return emptyMap()
        return data.roamingStorage[model]?.toMap() ?: emptyMap()
    }

    @Synchronized
    fun addStarModel(modelId: String): Boolean {
        val added = data.starModels.add(modelId)
        if (added) save()
        return added
    }

    @Synchronized
    fun removeStarModel(modelId: String): Boolean {
        val removed = data.starModels.remove(modelId)
        if (removed) save()
        return removed
    }

    @Synchronized
    fun isModelStarred(modelId: String): Boolean = data.starModels.contains(modelId)

    @Synchronized
    fun getStarModels(): Set<String> = data.starModels.toSet()

    @Synchronized
    fun clear() {
        data.modelId = null
        data.textureId = null
        data.roamingStorage.clear()
        data.starModels.clear()
        saveNow()
    }

    val modelId: String?
        get() = data.modelId

    val textureId: String?
        get() = data.textureId

    val hasSelection: Boolean
        get() = data.modelId != null && data.textureId != null
}
