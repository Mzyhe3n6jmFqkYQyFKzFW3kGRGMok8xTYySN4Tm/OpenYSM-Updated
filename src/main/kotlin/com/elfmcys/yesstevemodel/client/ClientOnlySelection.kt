package com.elfmcys.yesstevemodel.client

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.data.DeepCopy
import com.elfmcys.yesstevemodel.data.IsEmpty
import com.elfmcys.yesstevemodel.data.NbtLoad
import com.elfmcys.yesstevemodel.data.NbtSave
import com.elfmcys.yesstevemodel.util.getCompoundOrNull
import com.elfmcys.yesstevemodel.util.getListOrNull
import com.elfmcys.yesstevemodel.util.getStringOrNull
import com.google.gson.Gson
import com.google.gson.JsonObject
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.nbt.*
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

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
                    val starStr = starList.getString(i).orElse("")!!
                    if (starStr.isNotEmpty()) data.starModels.add(starStr)
                }
            }
            return data
        }
    }
}

@Environment(EnvType.CLIENT)
object ClientOnlySelection {
    private val FILE_NBT: Path = Constants.ConfigDir.resolve("client_selection.nbt")
    private val FILE_JSON: Path = Constants.ConfigDir.resolve("client_selection.json")

    private var data: ClientSelectionData = ClientSelectionData()

    @Volatile
    private var loaded: Boolean = false

    @Synchronized
    fun save(model: String?, texture: String?) {
        load()
        data.modelId = model
        data.textureId = texture
        persistToDisk()
    }

    @Synchronized
    fun updateRoamingVars(model: String?, vars: Map<String, Float>) {
        if (model.isNullOrEmpty() || vars.isEmpty()) return
        load()
        val map = data.roamingStorage.computeIfAbsent(model) { ConcurrentHashMap() }
        map.putAll(vars)
        persistToDisk()
    }

    @Synchronized
    fun getRoamingVars(model: String?): Map<String, Float> {
        if (model.isNullOrEmpty()) return emptyMap()
        load()
        return data.roamingStorage[model]?.toMap() ?: emptyMap()
    }

    @Synchronized
    fun addStarModel(modelId: String): Boolean {
        load()
        val added = data.starModels.add(modelId)
        if (added) persistToDisk()
        return added
    }

    @Synchronized
    fun removeStarModel(modelId: String): Boolean {
        load()
        val removed = data.starModels.remove(modelId)
        if (removed) persistToDisk()
        return removed
    }

    @Synchronized
    fun isModelStarred(modelId: String): Boolean {
        load()
        return data.starModels.contains(modelId)
    }

    @Synchronized
    fun getStarModels(): Set<String> {
        load()
        return data.starModels.toSet()
    }

    @Synchronized
    fun clear() {
        data = ClientSelectionData()
        loaded = false
    }

    private fun load() {
        if (loaded) return
        loadFromDisk()
    }

    @Synchronized
    private fun loadFromDisk() {
        if (loaded) return
        loaded = true
        runCatching {
            if (Files.exists(FILE_NBT)) {
                val tag = NbtIo.readCompressed(FILE_NBT, NbtAccounter.unlimitedHeap())
                data = ClientSelectionData.load(tag)
            } else if (Files.exists(FILE_JSON)) {
                loadFromLegacyJson()
                persistToDisk()
            }
        }.onFailure { e ->
            Constants.LOGGER.error("Failed to read client-only model selection", e)
        }
    }

    private fun loadFromLegacyJson() {
        runCatching {
            val json = Gson().fromJson(
                String(Files.readAllBytes(FILE_JSON), StandardCharsets.UTF_8),
                JsonObject::class.java
            ) ?: return
            if (json.has("model_id")) data.modelId = json.get("model_id").asString
            if (json.has("texture_id")) data.textureId = json.get("texture_id").asString
            data.roamingStorage.clear()
            if (json.has("roaming_storage")) {
                val roamingObj = json.getAsJsonObject("roaming_storage")
                for (mId in roamingObj.keySet()) {
                    val varsObj = roamingObj.getAsJsonObject(mId)
                    val varsMap = ConcurrentHashMap<String, Float>()
                    for (varName in varsObj.keySet()) {
                        varsMap[varName] = varsObj.get(varName).asFloat
                    }
                    data.roamingStorage[mId] = varsMap
                }
            }
        }.onFailure {
            Constants.LOGGER.error("Failed to read legacy client_selection.json", it)
        }
    }

    @Synchronized
    private fun persistToDisk() {
        runCatching {
            Files.createDirectories(FILE_NBT.parent)
            NbtIo.writeCompressed(data.save(), FILE_NBT)
        }.onFailure {
            Constants.LOGGER.error("Failed to save client-only model selection to NBT", it)
        }
    }

    val modelId: String?
        get() {
            load()
            return data.modelId
        }

    val textureId: String?
        get() {
            load()
            return data.textureId
        }

    val hasSelection: Boolean
        get() {
            load()
            return data.modelId != null && data.textureId != null
        }
}
