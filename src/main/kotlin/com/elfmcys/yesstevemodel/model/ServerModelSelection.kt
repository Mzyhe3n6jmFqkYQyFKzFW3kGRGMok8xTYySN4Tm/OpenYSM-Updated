package com.elfmcys.yesstevemodel.model

import com.elfmcys.yesstevemodel.Constants
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object ServerModelSelection {
    private val FILE: Path = Constants.ConfigDir.resolve("server_selection.json")
    private val GSON: Gson = Gson()

    class PlayerRecord {
        @Volatile
        var modelId: String? = null

        @Volatile
        var textureId: String? = null

        val roamingStorage: MutableMap<String, MutableMap<String, Float>> = ConcurrentHashMap()
    }

    private val players: MutableMap<String, PlayerRecord> = ConcurrentHashMap()

    @Volatile
    private var loaded: Boolean = false

    @Synchronized
    fun savePlayerSelection(uuid: UUID, model: String?, texture: String?) {
        load()
        val record = players.computeIfAbsent(uuid.toString()) { PlayerRecord() }
        record.modelId = model
        record.textureId = texture
        persistToDisk()
    }

    @Synchronized
    fun updateRoamingVars(uuid: UUID, model: String?, vars: Map<String, Float>) {
        if (model.isNullOrEmpty() || vars.isEmpty()) return
        load()
        val record = players.computeIfAbsent(uuid.toString()) { PlayerRecord() }
        val modelVars = record.roamingStorage.computeIfAbsent(model) { ConcurrentHashMap() }
        modelVars.putAll(vars)
        persistToDisk()
    }

    @Synchronized
    fun getRoamingVars(uuid: UUID, model: String?): Map<String, Float> {
        if (model.isNullOrEmpty()) return emptyMap()
        load()
        val record = players[uuid.toString()] ?: return emptyMap()
        return record.roamingStorage[model]?.toMap() ?: emptyMap()
    }

    @Synchronized
    fun getPlayerModel(uuid: UUID): String? {
        load()
        return players[uuid.toString()]?.modelId
    }

    @Synchronized
    fun getPlayerTexture(uuid: UUID): String? {
        load()
        return players[uuid.toString()]?.textureId
    }

    @Synchronized
    fun hasSelection(uuid: UUID): Boolean {
        load()
        val record = players[uuid.toString()] ?: return false
        return record.modelId != null && record.textureId != null
    }

    @Synchronized
    fun clear() {
        players.clear()
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
            if (!Files.exists(FILE)) return
            val json = GSON.fromJson(String(Files.readAllBytes(FILE), StandardCharsets.UTF_8), JsonObject::class.java)
                ?: return
            players.clear()
            val playersObj = if (json.has("players")) json.getAsJsonObject("players") else json
            for (uuidStr in playersObj.keySet()) {
                val playerElem = playersObj.get(uuidStr)
                if (!playerElem.isJsonObject) continue
                val playerObj = playerElem.asJsonObject
                val record = PlayerRecord()
                if (playerObj.has("model_id")) record.modelId = playerObj.get("model_id").asString
                if (playerObj.has("texture_id")) record.textureId = playerObj.get("texture_id").asString
                if (playerObj.has("roaming_storage")) {
                    val roamingObj = playerObj.getAsJsonObject("roaming_storage")
                    for (mId in roamingObj.keySet()) {
                        val varsObj = roamingObj.getAsJsonObject(mId)
                        val varsMap = ConcurrentHashMap<String, Float>()
                        for (varName in varsObj.keySet()) {
                            varsMap[varName] = varsObj.get(varName).asFloat
                        }
                        record.roamingStorage[mId] = varsMap
                    }
                }
                players[uuidStr] = record
            }
        }.onFailure { e ->
            Constants.LOGGER.error("Failed to read server model selection", e)
        }
    }

    @Synchronized
    private fun persistToDisk() {
        runCatching {
            Files.createDirectories(FILE.parent)
            val root = JsonObject()
            val playersObj = JsonObject()
            players.forEach { (uuidStr, record) ->
                val playerObj = JsonObject()
                playerObj.addProperty("model_id", record.modelId)
                playerObj.addProperty("texture_id", record.textureId)
                if (record.roamingStorage.isNotEmpty()) {
                    val roamingJson = JsonObject()
                    record.roamingStorage.forEach { (mId, vars) ->
                        val varsJson = JsonObject()
                        vars.forEach { (k, v) -> varsJson.addProperty(k, v) }
                        roamingJson.add(mId, varsJson)
                    }
                    playerObj.add("roaming_storage", roamingJson)
                }
                playersObj.add(uuidStr, playerObj)
            }
            root.add("players", playersObj)
            Files.write(FILE, GSON.toJson(root).toByteArray(StandardCharsets.UTF_8))
        }.onFailure {
            Constants.LOGGER.error("Failed to save server model selection", it)
        }
    }
}
