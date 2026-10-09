package com.elfmcys.yesstevemodel.client

import com.elfmcys.yesstevemodel.Constants
import com.google.gson.Gson
import com.google.gson.JsonObject
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

@Environment(EnvType.CLIENT)
object ClientOnlySelection {
    private val FILE: Path = Constants.ConfigDir.resolve("client_selection.json")
    private val GSON: Gson = Gson()

    @Volatile
    private var _modelId: String? = null

    @Volatile
    private var _textureId: String? = null

    @Volatile
    private var loaded: Boolean = false

    @Synchronized
    @JvmStatic
    fun save(model: String?, texture: String?) {
        _modelId = model
        _textureId = texture
        loaded = true
        runCatching {
            Files.createDirectories(FILE.parent)
            val json = JsonObject().apply {
                addProperty("model_id", model)
                addProperty("texture_id", texture)
            }
            Files.write(FILE, GSON.toJson(json).toByteArray(StandardCharsets.UTF_8))
        }.onFailure { e ->
            Constants.LOGGER.error("Failed to save client-only model selection", e)
        }
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
        }.onFailure { e ->
            Constants.LOGGER.error("Failed to read client-only model selection", e)
        }
    }

    @JvmStatic
    fun getModelId(): String? {
        load()
        return _modelId
    }

    @JvmStatic
    fun getTextureId(): String? {
        load()
        return _textureId
    }

    @JvmStatic
    fun hasSelection(): Boolean {
        load()
        return getModelId() != null && getTextureId() != null
    }
}
