package com.elfmcys.yesstevemodel.client

import com.elfmcys.yesstevemodel.Constants
import com.google.gson.Gson
import com.google.gson.JsonObject
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

@Environment(EnvType.CLIENT)
object ClientOnlySelection {
    private val FILE: Path = Constants.ConfigDir.resolve("client_selection.json")
    private val GSON: Gson = Gson()

    @Volatile
    private var _modelId: String? = null

    @Volatile
    private var _textureId: String? = null

    private val _roamingStorage: MutableMap<String, MutableMap<String, Float>> = ConcurrentHashMap()

    @Volatile
    private var loaded: Boolean = false

    @Synchronized
    fun save(model: String?, texture: String?) {
        load()
        _modelId = model
        _textureId = texture
        persistToDisk()
    }

    @Synchronized
    fun updateRoamingVars(model: String?, vars: Map<String, Float>) {
        if (model.isNullOrEmpty() || vars.isEmpty()) return
        load()
        val map = _roamingStorage.computeIfAbsent(model) { ConcurrentHashMap() }
        map.putAll(vars)
        persistToDisk()
    }

    @Synchronized
    fun getRoamingVars(model: String?): Map<String, Float> {
        if (model.isNullOrEmpty()) return emptyMap()
        load()
        return _roamingStorage[model]?.toMap() ?: emptyMap()
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
            if (!Files.exists(FILE)) return
            val json = GSON.fromJson(String(Files.readAllBytes(FILE), StandardCharsets.UTF_8), JsonObject::class.java)
                ?: return
            if (json.has("model_id")) _modelId = json.get("model_id").asString
            if (json.has("texture_id")) _textureId = json.get("texture_id").asString
            _roamingStorage.clear()
            if (json.has("roaming_storage")) {
                val roamingObj = json.getAsJsonObject("roaming_storage")
                for (mId in roamingObj.keySet()) {
                    val varsObj = roamingObj.getAsJsonObject(mId)
                    val varsMap = ConcurrentHashMap<String, Float>()
                    for (varName in varsObj.keySet()) {
                        varsMap[varName] = varsObj.get(varName).asFloat
                    }
                    _roamingStorage[mId] = varsMap
                }
            }
        }.onFailure { e ->
            Constants.LOGGER.error("Failed to read client-only model selection", e)
        }
    }

    @Synchronized
    private fun persistToDisk() {
        runCatching {
            Files.createDirectories(FILE.parent)
            val json = JsonObject().apply {
                addProperty("model_id", _modelId)
                addProperty("texture_id", _textureId)
                if (_roamingStorage.isNotEmpty()) {
                    val roamingJson = JsonObject()
                    _roamingStorage.forEach { (mId, vars) ->
                        val varsJson = JsonObject()
                        vars.forEach { (k, v) -> varsJson.addProperty(k, v) }
                        roamingJson.add(mId, varsJson)
                    }
                    add("roaming_storage", roamingJson)
                }
            }
            Files.write(FILE, GSON.toJson(json).toByteArray(StandardCharsets.UTF_8))
        }.onFailure {
            Constants.LOGGER.error("Failed to save client-only model selection", it)
        }
    }

    val modelId: String?
        get() {
            load()
            return _modelId
        }

    val textureId: String?
        get() {
            load()
            return _textureId
        }

    val hasSelection: Boolean
        get() {
            load()
            return modelId != null && textureId != null
        }
}
