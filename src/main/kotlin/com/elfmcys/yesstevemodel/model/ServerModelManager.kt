@file:Suppress("UnstableApiUsage", "unused")

package com.elfmcys.yesstevemodel.model

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.NameSpaces
import com.elfmcys.yesstevemodel.access.ServerCommonPacketListenerImplAccessor
import com.elfmcys.yesstevemodel.capability.AuthModelsCapability
import com.elfmcys.yesstevemodel.capability.ModelInfoCapability
import com.elfmcys.yesstevemodel.client.ExportResult
import com.elfmcys.yesstevemodel.config.ServerConfig
import com.elfmcys.yesstevemodel.model.format.ServerAnimationInfo
import com.elfmcys.yesstevemodel.model.format.ServerModelData
import com.elfmcys.yesstevemodel.model.format.UUIDComponentData
import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.network.message.S2CModelSyncPayload
import com.elfmcys.yesstevemodel.network.message.S2CSyncAuthModelsPacket
import com.elfmcys.yesstevemodel.resource.*
import com.elfmcys.yesstevemodel.resource.pojo.RawYsmModel
import com.elfmcys.yesstevemodel.util.YSMNativeHelper
import com.elfmcys.yesstevemodel.util.YSMThreadPool
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import com.google.common.util.concurrent.RateLimiter
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.netty.buffer.Unpooled
import it.unimi.dsi.fastutil.floats.FloatReferencePair
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.Connection
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraftforge.common.ForgeConfigSpec
import org.apache.commons.lang3.tuple.Pair
import rip.ysm.api.fabric.PlatformAPIImpl
import rip.ysm.legacy.YesModelUtils
import rip.ysm.security.YSMByteBuf
import rip.ysm.security.YsmCrypt
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Semaphore
import java.util.regex.Pattern
import kotlin.math.max
import kotlin.math.min

object ServerModelManager {
    @JvmField
    val BUILT: Path = Constants.ConfigDir.resolve("built")

    @JvmField
    val CUSTOM: Path = Constants.ConfigDir.resolve("custom")

    @JvmField
    val AUTH: Path = Constants.ConfigDir.resolve("auth")

    @JvmField
    val EXPORT: Path = Constants.ConfigDir.resolve("export")

    @JvmField
    val CACHE: Path = Constants.ConfigDir.resolve("cache")

    @JvmField
    val CACHE_SERVER_INDEX_FILE: Path = CACHE.resolve("server_index")

    @JvmField
    val CACHE_SERVER: Path = CACHE.resolve("server")

    @JvmField
    val CACHE_CLIENT: Path = CACHE.resolve("client")

    private var CACHE_NAME_INFO: Map<String, ServerModelData> = Maps.newHashMap()
    private var modelHashSet = IntOpenHashSet()
    private var AUTH_MODELS: Set<String> = Sets.newHashSet()
    private val syncStates = ConcurrentHashMap<UUID, PlayerSyncState>()
    private val packs = ConcurrentHashMap<String, ServerPackData>()
    private val theRandom = SecureRandom()

    @JvmField
    var serverKey: ByteArray? = null

    @Volatile
    private var initialized = false

    private var bandwidthLimiter: RateLimiter? = null
    private var threadLimiter: Semaphore? = null
    private var limitsInitialized = false

    private fun initRateLimit() {
        if (!limitsInitialized) {
            runCatching {
                val mbps = (ServerConfig.BANDWIDTH_LIMIT as? ForgeConfigSpec.ConfigValue<*>)?.get() as? Int ?: 5
                val bytesPerSec = max(1.0, mbps * 131072.0)
                bandwidthLimiter = RateLimiter.create(bytesPerSec)

                var threads = (ServerConfig.THREAD_COUNT as? ForgeConfigSpec.ConfigValue<*>)?.get() as? Int ?: 0
                if (threads <= 0) {
                    threads = max(2, Runtime.getRuntime().availableProcessors() - 1)
                }
                threadLimiter = Semaphore(threads)
                limitsInitialized = true
            }.onFailure {
                Constants.LOGGER.error("Failed to initialize limits from config", it)
                bandwidthLimiter = RateLimiter.create(5 * 131072.0)
                threadLimiter = Semaphore(max(2, Runtime.getRuntime().availableProcessors() - 1))
                limitsInitialized = true
            }
        }
    }

    class ServerPackData {
        var folderPath: String = ""
        var iconData: ByteArray? = null
        var iconWidth: Int = 0
        var iconHeight: Int = 0
        var iconFormat: Int = 0
        var name: String? = null
        var description: String? = null
        var lang: MutableMap<String, MutableMap<String, String>>? = null
    }

    private fun readResourceString(resourcePath: String): String? {
        return ServerModelManager::class.java.classLoader.getResourceAsStream(resourcePath)?.use {
            it.readBytes().toString(StandardCharsets.UTF_8)
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun reloadPacks() {
        CACHE_NAME_INFO = Maps.newHashMap()
        AUTH_MODELS = Sets.newHashSet()

        listOf(BUILT, CUSTOM, AUTH, EXPORT, CACHE, CACHE_SERVER, CACHE_CLIENT).forEach { createFolder(it) }

        extractBuiltinModels()

        val noticeContent = readResourceString("assets/yes_steve_model/notice.txt")
            ?: "This directory is cleared every time the game starts!\n该目录会在每次游戏启动时清空！"
        Files.writeString(BUILT.resolve("notice.txt"), noticeContent, StandardCharsets.UTF_8)

        val blacklistFile = Constants.ConfigDir.resolve("blacklist.txt")
        if (!Files.exists(blacklistFile)) {
            val blacklistContent = readResourceString("assets/yes_steve_model/blacklist.txt") ?: ""
            if (blacklistContent.isNotEmpty()) {
                Files.writeString(blacklistFile, blacklistContent, StandardCharsets.UTF_8)
            }
        }
        processBlacklist(blacklistFile)

        val serverIndex = CACHE_SERVER_INDEX_FILE
        val serverKeyBytes: ByteArray = if (Files.exists(serverIndex)) {
            runCatching {
                val jsonStr = Files.readString(serverIndex, StandardCharsets.UTF_8)
                val jsonElement = JsonParser.parseString(jsonStr).asJsonObject

                if (jsonElement.get("server_key") != null && jsonElement.get("server_key").asJsonPrimitive.isString) {
                    val decoded = Base64.getDecoder().decode(jsonElement.get("server_key").asString)
                    if (decoded.size != 56) {
                        throw IllegalStateException("ServerKey length must be 56 bytes, but got ${decoded.size}")
                    }
                    decoded
                } else {
                    generateAndSaveServerKey(serverIndex, jsonElement)
                }
            }.getOrElse {
                generateAndSaveServerKey(serverIndex, JsonObject())
            }
        } else {
            generateAndSaveServerKey(serverIndex, JsonObject())
        }

        serverKey = serverKeyBytes
        nativeLoadModels(null)
    }

    private fun generateAndSaveServerKey(serverIndex: Path, jsonElement: JsonObject): ByteArray {
        val key = ByteArray(56)
        SecureRandom().nextBytes(key)
        jsonElement.addProperty("server_key", Base64.getEncoder().encodeToString(key))
        Files.writeString(serverIndex, jsonElement.toString(), StandardCharsets.UTF_8)
        return key
    }

    private fun extractBuiltinModels() {
        if (Files.isDirectory(BUILT)) {
            runCatching {
                Files.walk(BUILT).use { s ->
                    s.sorted(Comparator.reverseOrder()).forEach { p ->
                        if (p != BUILT) {
                            runCatching { Files.deleteIfExists(p) }
                        }
                    }
                }
            }
        }
        try {
            val assetsBuiltin = FabricLoader.getInstance().getModContainer(NameSpaces.MOD())
                .flatMap { it.findPath("assets/" + NameSpaces.MOD() + "/builtin") }
                .orElse(null)

            if (assetsBuiltin == null || !Files.isDirectory(assetsBuiltin)) return

            Files.walk(assetsBuiltin).use { walker ->
                walker.forEach { src ->
                    try {
                        val relative = assetsBuiltin.relativize(src)
                        val dest = BUILT.resolve(relative.toString())
                        if (Files.isDirectory(src)) {
                            Files.createDirectories(dest)
                        } else {
                            dest.parent?.let { Files.createDirectories(it) }
                            Files.newInputStream(src).use { `in` ->
                                Files.copy(`in`, dest)
                            }
                        }
                    } catch (e: IOException) {
                        Constants.LOGGER.warn("Failed to extract builtin: ${src.fileName}", e)
                    }
                }
            }
        } catch (e: Exception) {
            Constants.LOGGER.error("Failed to extract builtin models", e)
        }
    }

    private fun processBlacklist(blacklistFile: Path) {
        val rules = ArrayList<Pattern>()
        try {
            Files.newBufferedReader(blacklistFile, StandardCharsets.UTF_8).use { reader ->
                reader.lineSequence()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() && !it.startsWith("#") }
                    .forEach { line ->
                        runCatching {
                            rules.add(Pattern.compile(line))
                        }
                    }
            }
        } catch (_: IOException) {
            return
        }

        if (rules.isEmpty() || !Files.isDirectory(BUILT)) return

        runCatching {
            Files.newDirectoryStream(BUILT).use { groups ->
                for (group in groups) {
                    if (!Files.isDirectory(group)) continue
                    var hasRemainingModels = false
                    Files.newDirectoryStream(group).use { models ->
                        for (model in models) {
                            if (!Files.isDirectory(model)) continue

                            val matchPath = "assets/yes_steve_model/builtin/${group.fileName}/${model.fileName}/"
                            var deleted = false
                            for (rule in rules) {
                                if (rule.matcher(matchPath).find()) {
                                    deleteRecursively(model)
                                    deleted = true
                                    break
                                }
                            }

                            if (!deleted) {
                                hasRemainingModels = true
                            }
                        }
                    }
                    if (!hasRemainingModels) {
                        deleteRecursively(group)
                    }
                }
            }
        }
    }

    private fun deleteRecursively(dir: Path) {
        if (!Files.isDirectory(dir)) {
            Files.deleteIfExists(dir)
            return
        }
        Files.newDirectoryStream(dir).use { entries ->
            for (entry in entries) {
                deleteRecursively(entry)
            }
        }
        Files.deleteIfExists(dir)
    }

    private fun createFolder(path: Path) {
        val folder = path.toFile()
        if (!folder.isDirectory)
            runCatching { Files.createDirectories(folder.toPath()) }.onFailure { it.printStackTrace() }
    }

    internal class PlayerSyncState {
        val clientKey = ByteArray(56)
        var key1: ByteArray? = null
        var clientNextKey: ByteArray? = null
        var step: Int = 0
        val allowedModels: MutableList<ServerModelData> = ArrayList()

        init {
            SecureRandom().nextBytes(clientKey)
        }
    }

    @JvmStatic
    fun nativeSendModelData(uuid: UUID, data: ByteBuffer?) {
        if (data != null && !data.hasRemaining() && data.position() > 0) {
            data.flip()
        }

        if (data == null || data.remaining() == 0) {
            syncStates.remove(uuid)
            return
        }

        val state = syncStates[uuid] ?: return

        runCatching {
            val packetBytes = ByteArray(data.remaining())
            data.get(packetBytes)

            when (state.step) {
                1 -> {
                    val key1 = state.key1 ?: return
                    val decrypted = YsmCrypt.decrypt(packetBytes, key1)
                    if (decrypted.size < 56) return

                    state.clientNextKey = decrypted.copyOfRange(decrypted.size - 56, decrypted.size)
                    val payload = decrypted.copyOfRange(0, decrypted.size - 56)

                    YSMByteBuf(Unpooled.wrappedBuffer(payload)).use { buf ->
                        buf.skipGarbageHeader()
                        if (buf.getRawBuf().readByte().toInt() != 0x02) return
                    }

                    state.step = 2
                    sendPacket03(uuid, state)
                }

                2 -> {
                    val key1 = state.key1 ?: return
                    val decrypted = YsmCrypt.decrypt(packetBytes, key1)

                    YSMByteBuf(Unpooled.wrappedBuffer(decrypted)).use { buf ->
                        buf.skipGarbageHeader()
                        if (buf.getRawBuf().readByte().toInt() != 0x04) return

                        val numRequests = buf.readVarInt()
                        val requestedHashes = ArrayList<LongArray>(numRequests)
                        for (i in 0 until numRequests) {
                            requestedHashes.add(longArrayOf(buf.readVarLong(), buf.readVarLong()))
                        }
                        state.step = 3
                        sendPacket05(uuid, state, requestedHashes)
                    }
                }
            }
        }.onFailure {
            Constants.LOGGER.error("Server sync error for $uuid", it)
        }
    }

    @JvmStatic
    fun nativeLoadModels(callback: ((ModelLoadResult) -> Unit)?): Boolean {
        return runCatching {
            val loadedModels = LinkedHashMap<String, ServerModelData>()
            val authIds = HashSet<String>()
            val validCacheFiles = HashSet<String>()

            packs.clear()
            listOf(BUILT, CUSTOM, AUTH).forEach { scanDirectoryPacks(it) }

            scanDirectoryModels(BUILT, CACHE_SERVER, loadedModels, authIds, validCacheFiles, false)
            scanDirectoryModels(CUSTOM, CACHE_SERVER, loadedModels, authIds, validCacheFiles, false)
            scanDirectoryModels(AUTH, CACHE_SERVER, loadedModels, authIds, validCacheFiles, true)

            runCatching {
                Files.list(CACHE_SERVER).use { stream ->
                    stream.forEach { file ->
                        if (!validCacheFiles.contains(file.fileName.toString())) {
                            runCatching { Files.deleteIfExists(file) }
                        }
                    }
                }
            }

            val result = ModelLoadResult(true, null, loadedModels, authIds.toTypedArray())
            AUTH_MODELS = authIds

            onModelLoadComplete(result, callback)
            true
        }.getOrElse {
            Constants.LOGGER.error("Model loading failed", it)
            false
        }
    }

    private fun scanDirectoryModels(
        baseDir: Path?,
        cacheDir: Path,
        loaded: MutableMap<String, ServerModelData>,
        authIds: MutableSet<String>,
        validCaches: MutableSet<String>,
        isAuth: Boolean
    ) {
        if (baseDir == null || !Files.isDirectory(baseDir)) return

        try {
            Files.walkFileTree(baseDir, object : SimpleFileVisitor<Path>() {
                override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                    if (dir == baseDir) return FileVisitResult.CONTINUE

                    runCatching {
                        if (!YSMFolderDeserializer.isModelFolder(dir)) return@runCatching
                        val modelId = baseDir.relativize(dir).toString().replace('\\', '/')
                        val rawModel = runCatching {
                            YSMFolderDeserializer(dir).use { it.deserialize() }
                        }.getOrElse {
                            Constants.LOGGER.error("Failed to load model folder at: $dir", it)
                            null
                        }

                        if (rawModel != null) {
                            val data = processAndCacheModel(modelId, rawModel, cacheDir, isAuth, validCaches)
                            if (data != null) {
                                loaded[modelId] = data
                                if (isAuth) authIds.add(modelId)
                            }
                        }

                        return FileVisitResult.SKIP_SUBTREE
                    }.onFailure {
                        Constants.LOGGER.error("Error checking directory: $dir", it)
                    }

                    return FileVisitResult.CONTINUE
                }

                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    if (!file.fileName.toString().endsWith(".ysm")) return FileVisitResult.CONTINUE

                    runCatching {
                        val modelId = baseDir.relativize(file).toString().replace('\\', '/')
                        val raw = Files.readAllBytes(file)
                        var rawModel = NativeParseUtil.parseNative(raw, modelId)

                        if (rawModel == null) {
                            val ysmCryptoVersion = YesModelUtils.getYsmCryptoVersion(raw)
                            rawModel = runCatching {
                                when (ysmCryptoVersion) {
                                    1, 2 -> {
                                        val input = YesModelUtils.input(raw)
                                        YSMFolderDeserializer(input).use { it.deserialize() }
                                    }

                                    3 -> {
                                        val decrypted = YsmCrypt.decryptYsmFile(raw)
                                        YSMBinaryDeserializer(decrypted).use { deserializer ->
                                            val parsed = deserializer.deserializeKeepOpen()
                                            deserializer.parseYSMFooter(parsed)
                                            parsed
                                        }
                                    }

                                    else -> null
                                }
                            }.getOrNull()
                        }

                        if (rawModel != null) {
                            val data = processAndCacheModel(modelId, rawModel, cacheDir, isAuth, validCaches)
                            if (data != null) {
                                loaded[modelId] = data
                                if (isAuth) authIds.add(modelId)
                            }
                        }
                    }.onFailure {
                        Constants.LOGGER.error("Failed to load binary model at: $file", it)
                    }
                    return FileVisitResult.CONTINUE
                }
            })
        } catch (e: IOException) {
            Constants.LOGGER.error("Failed to walk directory tree: $baseDir", e)
        }
    }

    private fun scanDirectoryPacks(baseDir: Path?) {
        if (baseDir == null || !Files.isDirectory(baseDir)) return
        try {
            Files.walk(baseDir, 1).use { stream ->
                stream.filter { Files.isDirectory(it) && it != baseDir }.forEach { path ->
                    val packJson = path.resolve("ysm-pack.json")
                    if (Files.exists(packJson)) {
                        runCatching {
                            val packData = ServerPackData()
                            packData.folderPath = baseDir.toFile().toURI().relativize(path.toFile().toURI()).path

                            val jsonStr = Files.readString(packJson, StandardCharsets.UTF_8)
                            val json = JsonParser.parseString(jsonStr).asJsonObject
                            if (json.has("name")) packData.name = json.get("name").asString
                            if (json.has("description")) packData.description = json.get("description").asString

                            if (json.has("lang") && json.get("lang").isJsonObject) {
                                val langMap = HashMap<String, MutableMap<String, String>>()
                                for ((langKey, langVal) in json.getAsJsonObject("lang").entrySet()) {
                                    if (langVal.isJsonObject) {
                                        val translations = HashMap<String, String>()
                                        for ((transKey, transVal) in langVal.asJsonObject.entrySet()) {
                                            translations[transKey] = transVal.asString
                                        }
                                        langMap[langKey] = translations
                                        val normalized = langKey.lowercase(Locale.ROOT).replace('-', '_')
                                        if (normalized != langKey) {
                                            langMap[normalized] = translations
                                        }
                                    }
                                }
                                packData.lang = langMap
                            }

                            val packPng = path.resolve("ysm-pack.png")
                            if (Files.exists(packPng)) {
                                val data = Files.readAllBytes(packPng)
                                val dims = getPngDimensions(data)
                                packData.iconData = data
                                packData.iconWidth = dims[0]
                                packData.iconHeight = dims[1]
                                packData.iconFormat = 2 // 2=PNG
                            }
                            packs[packData.folderPath] = packData
                        }.onFailure {
                            Constants.LOGGER.error("Failed to load pack metadata: $packJson", it)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Constants.LOGGER.error("Failed to walk directory for packs: $baseDir", e)
        }
    }

    private fun getPngDimensions(data: ByteArray?): IntArray {
        if (data == null || data.size < 24) return intArrayOf(0, 0)
        if (data[0].toInt() and 0xFF != 0x89 || data[1].toInt() != 0x50 || data[2].toInt() != 0x4E || data[3].toInt() != 0x47) {
            return intArrayOf(0, 0)
        }
        val width = ((data[16].toInt() and 0xFF) shl 24) or
                ((data[17].toInt() and 0xFF) shl 16) or
                ((data[18].toInt() and 0xFF) shl 8) or
                (data[19].toInt() and 0xFF)
        val height = ((data[20].toInt() and 0xFF) shl 24) or
                ((data[21].toInt() and 0xFF) shl 16) or
                ((data[22].toInt() and 0xFF) shl 8) or
                (data[23].toInt() and 0xFF)
        return intArrayOf(width, height)
    }

    private fun processAndCacheModel(
        modelId: String,
        model: RawYsmModel,
        serverCacheDir: Path,
        isAuth: Boolean,
        validCacheFiles: MutableSet<String>
    ): ServerModelData? {
        val sha256 = model.properties.sha256
        if (sha256.isEmpty()) return null

        return try {
            val currentServerKey = serverKey ?: return null
            val hashes = YsmCrypt.calculateModelHashes(sha256, currentServerKey)
            val cacheFileName = String.format("%016x%016x", hashes[0], hashes[1])
            val cacheFile = serverCacheDir.resolve(cacheFileName)
            if (!serverCacheDir.toFile().isDirectory) {
                Files.createDirectories(serverCacheDir)
            }
            var needsUpdate = true
            if (Files.exists(cacheFile)) {
                val existingData = Files.readAllBytes(cacheFile)
                if (YsmCrypt.verifyServerCache(existingData, hashes[0], hashes[1])) {
                    needsUpdate = false
                }
            }
            if (needsUpdate) {
                val encryptedCache = YSMBinarySerializer.serialize(model, 32, true).use { serialized ->
                    val raw = serialized.getRawBuf()
                    if (raw.hasArray()) {
                        val off = raw.arrayOffset() + raw.readerIndex()
                        val len = raw.readableBytes()
                        YsmCrypt.encryptServerCache(raw.array(), off, len, currentServerKey, hashes[0], hashes[1])
                    } else {
                        YsmCrypt.encryptServerCache(serialized.toArray(), currentServerKey, hashes[0], hashes[1])
                    }
                }
                Files.write(cacheFile, encryptedCache)
            }
            validCacheFiles.add(cacheFileName)

            val isCustomSkinModel = "misc/2_steve" == modelId || "misc/1_alex" == modelId

            mapToDataClass(modelId, model, isAuth, isCustomSkinModel)
        } catch (e: Exception) {
            Constants.LOGGER.error("Failed to process and cache model: $modelId", e)
            null
        }
    }

    private fun mapToDataClass(
        modelId: String,
        raw: RawYsmModel,
        isAuth: Boolean,
        isCustomSkinModel: Boolean
    ): ServerModelData {
        val serverModelInfo = YSMClientMapper.buildModelInfo(raw)
        val animMap = HashMap<String, Array<String>>()
        for ((key, value) in raw.mainEntity.animationFiles) {
            animMap[key] = value.animations.keys.toTypedArray()
        }
        val texArr = raw.mainEntity.textures.keys.toTypedArray()
        val animInfo = ServerAnimationInfo(animMap, texArr)

        val projectiles = raw.projectiles.values.map { v ->
            v.matchIds ?: arrayOf(v.identifier ?: "")
        }
        val vehicles = raw.vehicles.values.map { v ->
            v.matchIds ?: arrayOf(v.identifier ?: "")
        }
        return ServerModelData(modelId, animInfo, projectiles, vehicles, serverModelInfo, isCustomSkinModel, isAuth)
    }

    @JvmStatic
    fun nativeSyncModels(
        uuids: Array<UUID>,
        playerNames: Array<String>,
        modelIds: Array<String>,
        callback: ((UUIDComponentData) -> Unit)?
    ) {
        initRateLimit()
        YSMThreadPool.launchSync {
            runCatching {
                PlatformAPIImpl.getServer() ?: return@launchSync

                for (uuid in uuids) {
                    val state = syncStates.computeIfAbsent(uuid) { PlayerSyncState() }
                    state.allowedModels.clear()
                    state.allowedModels.addAll(CACHE_NAME_INFO.values)
                    state.step = 1

                    val garbageLen = 16 + theRandom.nextInt(48)
                    val garbage = ByteArray(garbageLen)
                    theRandom.nextBytes(garbage)

                    YSMByteBuf(Unpooled.buffer()).use { outBuf ->
                        outBuf.writeGarbageHeader(garbageLen, garbage)
                        outBuf.writeByte(0x01.toByte())
                        val result = YsmCrypt.encrypt(outBuf.toArray(), YsmCrypt.publicKey, true)
                        state.key1 = result.nextKey()

                        sendModelData(uuid, ByteBuffer.wrap(result.data()), PendingTransfer())
                    }
                }
            }.onFailure {
                Constants.LOGGER.error("Sync initiation failed", it)
            }
        }
    }

    private fun sendPacket03(uuid: UUID, state: PlayerSyncState) {
        val currentServerKey = serverKey ?: return
        val clientNextKey = state.clientNextKey ?: return
        val garbageLen = 16 + theRandom.nextInt(48)
        val garbage = ByteArray(garbageLen)
        theRandom.nextBytes(garbage)

        try {
            YSMByteBuf(Unpooled.buffer()).use { outBuf ->
                outBuf.writeGarbageHeader(garbageLen, garbage)

                outBuf.writeVarInt(3) // Type
                outBuf.writeVarLong(0L) // Cache folder hash

                outBuf.getRawBuf().writeBytes(currentServerKey)
                outBuf.getRawBuf().writeBytes(state.clientKey)

                outBuf.writeVarInt(state.allowedModels.size)
                for (model in state.allowedModels) {
                    val sha256 = model.getLoadedModelData().modelHash
                    val hashes = YsmCrypt.calculateModelHashes(sha256, currentServerKey)
                    outBuf.writeVarLong(hashes[0])
                    outBuf.writeVarLong(hashes[1])
                    outBuf.writeString(model.modelId)
                    outBuf.writeVarInt(if (model.isAuth()) 1 else 0)
                    outBuf.writeVarInt(if (model.isCustomSkinModel()) 1 else 0)
                    outBuf.writeVarInt(32) // format
                }

                outBuf.writeVarInt(packs.size)
                for (pack in packs.values) {
                    outBuf.writeString(pack.folderPath)

                    if (pack.iconData != null) {
                        outBuf.writeVarInt(1)
                        outBuf.writeByteArray(pack.iconData)
                        outBuf.writeVarInt(pack.iconWidth)
                        outBuf.writeVarInt(pack.iconHeight)
                        outBuf.writeVarInt(pack.iconFormat)
                        outBuf.writeVarInt(1)
                    } else {
                        outBuf.writeVarInt(0)
                    }

                    if (pack.name != null || pack.description != null) {
                        outBuf.writeVarInt(1)
                        outBuf.writeString(pack.name ?: "")
                        outBuf.writeString(pack.description ?: "")
                    } else {
                        outBuf.writeVarInt(0)
                    }

                    val packLang = pack.lang
                    if (!packLang.isNullOrEmpty()) {
                        outBuf.writeVarInt(packLang.size)
                        for ((langKey, langVal) in packLang) {
                            outBuf.writeString(langKey)
                            outBuf.writeVarInt(langVal.size)
                            for ((k, v) in langVal) {
                                outBuf.writeString(k)
                                outBuf.writeString(v)
                            }
                        }
                    } else {
                        outBuf.writeVarInt(0)
                    }
                }

                outBuf.writeVarInt(0)

                val result = YsmCrypt.encrypt(outBuf.toArray(), clientNextKey, false)
                sendModelData(uuid, ByteBuffer.wrap(result.data()), PendingTransfer())
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun sendPacket05(uuid: UUID, state: PlayerSyncState, requestedHashes: List<LongArray>) {
        YSMThreadPool.launchSync {
            try {
                threadLimiter?.acquire()

                val transfer = PendingTransfer()

                for (hashes in requestedHashes) {
                    val hash1 = hashes[0]
                    val hash2 = hashes[1]
                    val fileName = String.format("%016x%016x", hash1, hash2)
                    val file = CACHE_SERVER.resolve(fileName)

                    if (!Files.exists(file)) continue

                    val fileData = Files.readAllBytes(file)
                    val totalSize = fileData.size
                    val maxChunkSize = 30720
                    val chunkCount = (totalSize + maxChunkSize - 1) / maxChunkSize
                    val chunkSize = (totalSize + chunkCount - 1) / chunkCount

                    var offset = 0

                    while (offset < totalSize) {
                        val length = min(chunkSize, totalSize - offset)

                        val garbageLen = 16 + theRandom.nextInt(48)
                        val garbage = ByteArray(garbageLen)
                        theRandom.nextBytes(garbage)

                        YSMByteBuf(Unpooled.buffer()).use { outBuf ->
                            outBuf.writeGarbageHeader(garbageLen, garbage)
                            outBuf.writeVarInt(5) // Type
                            outBuf.writeVarLong(hash1)
                            outBuf.writeVarLong(hash2)
                            outBuf.writeVarInt(totalSize)
                            outBuf.writeVarInt(offset)
                            outBuf.writeVarInt(length)
                            outBuf.getRawBuf().writeBytes(fileData, offset, length)
                            val key1 = state.key1
                            val result = if (key1 != null) {
                                YsmCrypt.encrypt(outBuf.toArray(), key1, false)
                            } else null

                            if (result != null) {
                                val success = sendModelData(uuid, ByteBuffer.wrap(result.data()), transfer)
                                if (success) {
                                    offset += length
                                } else {
                                    Thread.yield()
                                }
                            } else {
                                break
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Constants.LOGGER.error("Failed to send model chunks to $uuid", e)
            } finally {
                threadLimiter?.release()
            }
        }
    }

    @JvmStatic
    fun nativeExportModel(modelID: String, extra: String?, callback: ((ExportResult) -> Unit)?) {
        YSMThreadPool.launch {
            try {
                val modelData = CACHE_NAME_INFO[modelID]
                if (modelData == null) {
                    val msg = YSMNativeHelper.createTranslatableComponent(
                        "commands.yes_steve_model.export.failure",
                        arrayOf(": $modelID\n Model not found")
                    ) as? Component ?: Component.literal("Model not found: $modelID")
                    callback?.invoke(
                        ExportResult(
                            false,
                            msg,
                            "",
                            "",
                            0
                        )
                    )
                    return@launch
                }

                val currentServerKey = serverKey
                if (currentServerKey == null) {
                    callback?.invoke(
                        ExportResult(
                            false,
                            Component.literal("Server key not initialized"),
                            "",
                            "",
                            0
                        )
                    )
                    return@launch
                }

                val sha256 = modelData.getLoadedModelData().modelHash
                val hashes = YsmCrypt.calculateModelHashes(sha256, currentServerKey)
                val cacheFileName = String.format("%016x%016x", hashes[0], hashes[1])
                val cacheFile = CACHE_SERVER.resolve(cacheFileName)

                if (!Files.exists(cacheFile)) {
                    callback?.invoke(
                        ExportResult(
                            false,
                            Component.literal("Cache file missing for: $modelID"),
                            "",
                            "",
                            0
                        )
                    )
                    return@launch
                }

                val cacheData = Files.readAllBytes(cacheFile)
                val clearText = YsmCrypt.read(cacheData, currentServerKey)

                val coreDataLength: Int
                YSMBinaryDeserializer(clearText, 32).use { deserializer ->
                    deserializer.deserializeKeepOpen()
                    coreDataLength = deserializer.reader.getOffset()
                }

                YSMByteBuf(Unpooled.buffer()).use { outBuf ->
                    outBuf.writeDword(32)
                    outBuf.getRawBuf().writeBytes(clearText, 0, coreDataLength)
                    outBuf.writeVarInt(32) // version
                    outBuf.writeVarInt(1)
                    val randBytes = ByteArray(8)
                    theRandom.nextBytes(randBytes)
                    val sb = StringBuilder(16)
                    for (b in randBytes) {
                        sb.append(String.format("%02x", b))
                    }
                    outBuf.writeString(sb.toString())
                    outBuf.writeVarLong(java.time.Instant.now().epochSecond)
                    outBuf.writeString(extra ?: "")
                    outBuf.writeVarInt(0)
                    val rawBytes = ByteArray(outBuf.getRawBuf().readableBytes())
                    outBuf.getRawBuf().readBytes(rawBytes)
                    val finalEncrypted = YsmCrypt.encryptYsmFile(rawBytes)
                    val exportPath = EXPORT.resolve("$modelID.ysm")
                    exportPath.parent?.let { Files.createDirectories(it) }
                    Files.write(exportPath, finalEncrypted)
                    callback?.invoke(
                        ExportResult(
                            true,
                            Component.empty(),
                            Paths.get("export", "$modelID.ysm").toString(),
                            "",
                            0
                        )
                    )
                }
            } catch (e: Exception) {
                callback?.invoke(
                    ExportResult(
                        false,
                        Component.literal("Export failed: ${e.message}"),
                        "",
                        "",
                        0
                    )
                )
            }
        }
    }

    @JvmStatic
    fun getModelDefinition(str: String): Optional<ServerModelData> {
        return Optional.ofNullable(CACHE_NAME_INFO[str])
    }

    @JvmStatic
    operator fun get(str: String): ServerModelData? {
        return CACHE_NAME_INFO[str]
    }

    @JvmStatic
    fun getServerModelInfo(): Map<String, ServerModelData> {
        return CACHE_NAME_INFO
    }

    @JvmStatic
    fun getAuthModels(): Set<String> {
        return AUTH_MODELS
    }

    @JvmStatic
    fun requestPlayerAuth(serverPlayer: ServerPlayer, consumer: ((UUIDComponentData) -> Unit)? = null) {
        val currentServer = PlatformAPIImpl.getServer() ?: return
        currentServer.execute {
            val players = currentServer.playerList.players
            val arrayList = ArrayList<FloatReferencePair<ServerPlayer>>()
            for (serverPlayer2 in players) {
                if (serverPlayer2.level().dimensionType() === serverPlayer.level().dimensionType()) {
                    arrayList.add(FloatReferencePair.of(serverPlayer2.distanceTo(serverPlayer), serverPlayer2))
                }
            }
            arrayList.sortWith { a, b -> a.firstFloat().compareTo(b.firstFloat()) }
            nativeSyncModels(
                arrayOf(serverPlayer.uuid),
                arrayOf(serverPlayer.gameProfile.name),
                collectPlayerModelIds(arrayList.map { it.second() }),
                consumer
            )
        }
    }

    @JvmStatic
    fun loadModels(
        consumer: ((ModelLoadResult) -> Unit)? = null,
        consumer2: ((UUIDComponentData) -> Unit)? = null
    ): Boolean {
        val action: (ModelLoadResult) -> Unit = { modelLoadResult ->
            consumer?.invoke(modelLoadResult)
            val currentServer = PlatformAPIImpl.getServer()
            currentServer?.execute {
                val players = currentServer.playerList.players
                for (value in players) {
                    validatePlayerModel(value)
                }
                nativeSyncModels(
                    players.filter { NetworkHandler.isPlayerConnected(it) }.map { it.uuid }.toTypedArray(),
                    players.filter { NetworkHandler.isPlayerConnected(it) }.map { it.gameProfile.name }.toTypedArray(),
                    collectPlayerModelIds(players),
                    consumer2
                )
            }
        }
        return nativeLoadModels(action)
    }

    private fun collectPlayerModelIds(collection: Collection<ServerPlayer>): Array<String> {
        return collection.asSequence()
            .filter { NetworkHandler.isPlayerConnected(it) }
            .mapNotNull { serverPlayer ->
                ModelInfoCapability[serverPlayer]?.getModelId()
            }
            .distinct()
            .toList()
            .toTypedArray()
    }

    private fun onModelLoadComplete(modelLoadResult: ModelLoadResult, callback: ((ModelLoadResult) -> Unit)?) {
        val currentServer = PlatformAPIImpl.getServer()
        initialized = true
        if (currentServer != null) {
            currentServer.execute {
                if (modelLoadResult.isSuccess) {
                    val intOpenHashSet = IntOpenHashSet(modelLoadResult.modelDefinitions.size)
                    for (data in modelLoadResult.modelDefinitions.values) {
                        intOpenHashSet.add(data.getLoadedModelData().hashId)
                    }
                    CACHE_NAME_INFO = modelLoadResult.modelDefinitions
                    modelHashSet = intOpenHashSet
                    AUTH_MODELS = modelLoadResult.authModelIds
                }
                if (callback != null) {
                    YSMThreadPool.launch { callback(modelLoadResult) }
                }
            }
            return
        }
        if (modelLoadResult.isSuccess) {
            CACHE_NAME_INFO = modelLoadResult.modelDefinitions
            AUTH_MODELS = modelLoadResult.authModelIds
        }
        callback?.invoke(modelLoadResult)
    }

    @JvmStatic
    fun syncModelToPlayer(uuid: UUID) {
        nativeSendModelData(uuid, null)
    }

    private fun getPlayerConnection(uuid: UUID): Connection? {
        val currentServer = PlatformAPIImpl.getServer() ?: return null
        val player = currentServer.playerList.getPlayer(uuid) ?: return null
        val serverGamePacketListenerImpl = player.connection
        if (!serverGamePacketListenerImpl.isAcceptingMessages || serverGamePacketListenerImpl.javaClass != ServerGamePacketListenerImpl::class.java)
            return null
        return (serverGamePacketListenerImpl as ServerCommonPacketListenerImplAccessor).`ysm$getConnection`()
    }

    private fun sendModelData(uuid: UUID, byteBuffer: ByteBuffer, pendingTransfer: PendingTransfer): Boolean {
        val connection = getPlayerConnection(uuid)
        return connection != null && sendPacketReliably(
            connection,
            NetworkHandler.toClientboundPacket(S2CModelSyncPayload(byteBuffer)),
            pendingTransfer
        )
    }

    private fun sendPacketReliably(
        connection: Connection,
        packet: Packet<*>,
        pendingTransfer: PendingTransfer
    ): Boolean {
        if (!connection.isConnected) return false
        runCatching {
            connection.send(packet)
            return true
        }.onFailure {
            Constants.LOGGER.error("Failed to send packet: ${it.localizedMessage}", it)
            return false
        }
        return false
    }

    @JvmStatic
    fun getDefaultModelConfig(): Pair<String, String> {
        val defaultModelId =
            (ServerConfig.DEFAULT_MODEL_ID as? ForgeConfigSpec.ConfigValue<*>)?.get() as? String ?: "default"
        var defaultTexture =
            (ServerConfig.DEFAULT_MODEL_TEXTURE as? ForgeConfigSpec.ConfigValue<*>)?.get() as? String ?: "default"
        if (defaultTexture.lowercase().endsWith(".png") && defaultTexture.length > 4) {
            defaultTexture = defaultTexture.substring(0, defaultTexture.length - 4)
        }
        if (!initialized) return Pair.of(defaultModelId, defaultTexture)
        val modelData = CACHE_NAME_INFO[defaultModelId] ?: return Pair.of("default", "default")
        if (!modelData.modelInfo.textures.contains(defaultTexture)) {
            defaultTexture = if (modelData.modelInfo.textures
                    .contains(modelData.getLoadedModelData().modelProperties.defaultTexture)
            ) modelData.getLoadedModelData().modelProperties.defaultTexture else if (modelData.modelInfo.textures.isEmpty()
            ) "" else modelData.modelInfo.textures[0]
        }
        return Pair.of(defaultModelId, defaultTexture)
    }

    @JvmStatic
    fun validatePlayerModel(serverPlayer: ServerPlayer) {
        if (CACHE_NAME_INFO.isNotEmpty()) {
            val modelInfoCap = ModelInfoCapability[serverPlayer] ?: return
            val authModelsCap = AuthModelsCapability[serverPlayer] ?: return
            if (authModelsCap.getAuthModels().removeIf { str -> !CACHE_NAME_INFO.containsKey(str) }) {
                NetworkHandler.sendToClientPlayer(S2CSyncAuthModelsPacket(authModelsCap.getAuthModels()), serverPlayer)
            }
            val modelId = modelInfoCap.getModelId()
            if (!getServerModelInfo().containsKey(modelId) || ((AUTH_MODELS.contains(modelId) && !authModelsCap.getAuthModels()
                    .contains(modelInfoCap.getModelId())) || !(CACHE_NAME_INFO[modelId] ?: return).modelInfo.textures
                    .contains(modelInfoCap.getSelectTexture()))
            ) {
                modelInfoCap.resetToDefault()
            }
            modelInfoCap.retainAnimationKeys(modelHashSet)
        }
    }

    private class PendingTransfer {
        var pendingBytes: Long = 0
        var hasStarted: Boolean = false
    }
}
