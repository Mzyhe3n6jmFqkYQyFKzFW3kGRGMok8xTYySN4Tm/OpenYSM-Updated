@file:Suppress("unused")

package com.elfmcys.yesstevemodel.client

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.NativeLibLoader
import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.client.gui.IGuiWidget
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.client.model.ModelAssemblyFactory
import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture
import com.elfmcys.yesstevemodel.client.upload.IResourceLocatable
import com.elfmcys.yesstevemodel.client.upload.UploadManager
import com.elfmcys.yesstevemodel.model.ServerModelManager
import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.network.message.C2SModelSyncPayload
import com.elfmcys.yesstevemodel.resource.YSMBinaryDeserializer
import com.elfmcys.yesstevemodel.resource.YSMClientMapper
import com.elfmcys.yesstevemodel.resource.YSMFolderDeserializer
import com.elfmcys.yesstevemodel.resource.models.ModelPackData
import com.elfmcys.yesstevemodel.resource.pojo.RawYsmModel
import com.elfmcys.yesstevemodel.util.FileTypeUtil
import com.elfmcys.yesstevemodel.util.YSMThreadPool
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap
import com.mojang.blaze3d.systems.RenderSystem
import io.netty.buffer.Unpooled
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap
import kotlinx.coroutines.*
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.Minecraft
import net.minecraft.network.Connection
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.tuple.Pair
import org.apache.logging.log4j.message.StringFormattedMessage
import rip.ysm.security.YSMByteBuf
import rip.ysm.security.YSMClientCache
import rip.ysm.security.YsmCrypt
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.file.*
import java.security.SecureRandom
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

@Environment(EnvType.CLIENT)
object ClientModelManager {
    private var syncStep: Int = 1
    private var key1: ByteArray? = null
    private var lastKey: ByteArray? = null
    private var serverKey: ByteArray? = null
    private var clientKey: ByteArray? = null
    private var currentCacheFolderName: String? = null
    private val pendingModelsCount: AtomicInteger = AtomicInteger(0)

    private val modelParseJob = SupervisorJob()
    private val modelParseScope = CoroutineScope(
        modelParseJob + Dispatchers.IO.limitedParallelism(1) + CoroutineName("YSM-Model-Parse")
    )

    private val serverModels: MutableMap<UUID, ServerModelContext> = ConcurrentHashMap()
    private val SECURE_RANDOM: SecureRandom = SecureRandom()

    @Volatile
    private var _localModelContext: ModelAssembly? = null

    @Volatile
    private var pendingModelCallback: (() -> Unit)? = null

    private var _defaultTexture: IResourceLocatable? = null

    @Volatile
    private var serverConnection: Connection? = null

    @Volatile
    var modelAssemblyMap: Map<String, ModelAssembly> = Object2ReferenceMaps.emptyMap()
        private set

    @Volatile
    var modelPackMap: Map<String, ModelPackData> = Object2ReferenceOpenHashMap()
        private set

    private val pendingModelQueue: ConcurrentLinkedQueue<Pair<ModelAssembly, String>> = ConcurrentLinkedQueue()
    private val guiWidgets: WeakHashMap<IGuiWidget, Any?> = WeakHashMap()
    private val syncState: SyncStatus = SyncStatus()

    private var isOysmServer: Boolean = false
    private var allowUpload: Boolean = false

    private val cachedModelHashes: MutableList<ModelHash> = ArrayList()

    enum class SyncState {
        WAITING,
        LOADING,
        IDLE,
        PREPARING,
        SYNCING
    }

    @Suppress("MemberVisibilityCanBePrivate")
    data class ServerModelContext(
        val hash1: Long,
        val hash2: Long,
        val modelId: String,
        val isAuth: Boolean,
        val isCustomSkinModel: Int,
        val version: Int
    ) {
        val uuid: UUID = UUID(hash1, hash2)
        var fileBuffer: ByteArray? = null
        var totalSize: Int = 0
        var bytesReceived: Int = 0
    }

    data class ModelHash(val hash1: Long, val hash2: Long)

    class SyncStatus {
        var currentState: SyncState = SyncState.WAITING
            private set
        var totalModels: Int = -1
            private set
        var syncedModels: Int = -1

        fun setState(syncState: SyncState) {
            Constants.LOGGER.info("Sync state: {}", syncState)
            currentState = syncState
            totalModels = -1
            syncedModels = -1
        }

        fun startSyncing(totalModels: Int) {
            currentState = SyncState.SYNCING
            this.totalModels = totalModels
            syncedModels = 0
        }
    }

    fun loadDefaultModel() {
        Constants.LOGGER.info("Loading builtin default model...")
        runCatching {
            val resourcePath = "/assets/yes_steve_model/builtin/default"
            val resourceUrl = YesSteveModel::class.java.getResource(resourcePath) ?: run {
                Constants.LOGGER.error("Builtin default model not found in classpath: {}", resourcePath)
                return
            }

            val uri = resourceUrl.toURI()
            var jarFs: FileSystem?
            val defaultPath: Path = if ("jar" == uri.scheme) {
                jarFs = runCatching {
                    FileSystems.getFileSystem(uri)
                }.getOrElse {
                    FileSystems.newFileSystem(uri, emptyMap<String, Any>())
                }
                jarFs.getPath(resourcePath)
            } else {
                Paths.get(uri)
            }

            runCatching {
                YSMFolderDeserializer(defaultPath).use { deserializer ->
                    val rawModel: RawYsmModel = deserializer.deserialize()
                    val parsedBundle: ClientModelInfo = YSMClientMapper.buildParsedBundle(rawModel, "default")
                    onModelDataReceived(parsedBundle, "default", isPrimary = true, isAuth = false)
                    Constants.LOGGER.info("Successfully pushed Default Model to render queue.")
                }
            }.onFailure { e ->
                Constants.LOGGER.error("Failed to dispatch Default Model", e)
            }
        }.onFailure { e ->
            Constants.LOGGER.error("Failed to load builtin default model", e)
        }
    }

    private fun processServerData(data: ByteBuffer?) {
        if (data == null) {
            resetClientState()
            return
        }
        runCatching {
            if (!data.hasRemaining() && data.position() > 0) {
                data.flip()
            }
            if (!data.hasRemaining()) return

            val packetBytes = ByteArray(data.remaining())
            data.get(packetBytes)

            when (syncStep) {
                1 -> {
                    val decrypted = YsmCrypt.decrypt(packetBytes, YsmCrypt.publicKey)
                    handlePacket01(decrypted)
                }

                2 -> {
                    val currentLastKey = lastKey
                    if (currentLastKey != null) {
                        val decrypted = YsmCrypt.decrypt(packetBytes, currentLastKey)
                        YSMByteBuf(Unpooled.wrappedBuffer(decrypted)).use { buf ->
                            handlePacket03(buf)
                        }
                    }
                }

                3 -> {
                    val currentKey1 = key1
                    if (currentKey1 != null) {
                        val decrypted = YsmCrypt.decrypt(packetBytes, currentKey1)
                        YSMByteBuf(Unpooled.wrappedBuffer(decrypted)).use { buf ->
                            handlePacket05(buf)
                        }
                    }
                }
            }
        }.onFailure { e ->
            Constants.LOGGER.error("Sync Error at step $syncStep", e)
        }
    }

    private fun handlePacket01(decryptedBuffer: ByteArray) {
        val newKey1 = ByteArray(56)
        System.arraycopy(decryptedBuffer, decryptedBuffer.size - 56, newKey1, 0, 56)
        key1 = newKey1
        syncStep = 2

        Constants.LOGGER.info("Exchanged Key1. Preparing to send Packet 02.")
        onSyncProgress(-1)

        val garbageLen = 16 + SECURE_RANDOM.nextInt(48)
        val garbage = ByteArray(garbageLen)
        SECURE_RANDOM.nextBytes(garbage)

        YSMByteBuf(Unpooled.buffer()).use { outBuf ->
            outBuf.writeGarbageHeader(garbageLen, garbage)
            outBuf.rawBuf.writeByte(0x02)
            outBuf.rawBuf.writeByte(0x00)

            val result = YsmCrypt.encrypt(outBuf.toArray(), newKey1, true)
            lastKey = result.nextKey()
            sendModelFile(ByteBuffer.wrap(result.data()))
        }
    }

    @Suppress("UnusedVariable")
    private fun handlePacket03(buf: YSMByteBuf) {
        buf.skipGarbageHeader()
        val type = buf.readVarInt()
        val folderHash = buf.readVarLong()
        currentCacheFolderName = java.lang.Long.toHexString(folderHash)

        val newServerKey = ByteArray(56)
        buf.rawBuf.readBytes(newServerKey)
        serverKey = newServerKey

        val newClientKey = ByteArray(56)
        buf.rawBuf.readBytes(newClientKey)
        clientKey = newClientKey

        val cacheDir = ServerModelManager.CACHE_CLIENT.resolve(currentCacheFolderName ?: "default_cache").toFile()
        if (!cacheDir.exists()) cacheDir.mkdirs()

        val localCacheMap = YSMClientCache.buildCacheIndex(cacheDir, newClientKey)
        val modelsToRequest = ArrayList<ModelHash>()

        val unkSize = buf.readVarInt()
        onSyncProgress(unkSize)

        val validServerModelIds = HashSet<String>()
        val previousModelIds = ArrayList<String>()
        val updatedModelIds = ArrayList<String>()
        val isModelReadyList = ArrayList<Boolean>()

        for (i in 0 until unkSize) {
            val hash1 = buf.readVarLong()
            val hash2 = buf.readVarLong()
            val mHash = ModelHash(hash1, hash2)
            cachedModelHashes.add(mHash)

            val modelId = buf.readString()
            val isAuth = buf.readVarInt() == 1
            val isCustomSkinModel = buf.readVarInt()
            val version = buf.readVarInt()

            val ctx = ServerModelContext(hash1, hash2, modelId, isAuth, isCustomSkinModel, version)
            serverModels[ctx.uuid] = ctx
            validServerModelIds.add(modelId)

            val cachedFile = localCacheMap[ctx.uuid]
            val isFileValid = cachedFile != null && YSMClientCache.verifyFileContent(cachedFile, hash1, hash2)

            val alreadyInMemory = modelAssemblyMap.containsKey(modelId)

            if (isFileValid) {
                Constants.LOGGER.info("Cache HIT & Validated: {}", ctx.uuid)
                if (alreadyInMemory) {
                    previousModelIds.add(modelId)
                    updatedModelIds.add(modelId)
                    isModelReadyList.add(isAuth)
                } else {
                    modelParseScope.launch {
                        val currentKey = clientKey ?: return@launch
                        runCatching {
                            val fileBytes = Files.readAllBytes(cachedFile.toPath())
                            val decompressed = YsmCrypt.read(fileBytes, currentKey)
                            parseAndLoadModel(decompressed, modelId, isAuth)
                        }.onFailure { e ->
                            if (e is CancellationException) return@onFailure
                            Constants.LOGGER.error("Failed to parse and load cached model: $modelId", e)
                        }
                    }
                }
            } else {
                Constants.LOGGER.info("Cache MISS or Invalid: {} -> Requesting...", ctx.uuid)
                modelsToRequest.add(mHash)
            }
        }

        val unkSize2 = buf.readVarInt()
        val parsedPacks = ArrayList<ModelPackData>()

        for (i in 0 until unkSize2) {
            val folderPath = buf.readString()
            var iconTexture: OuterFileTexture? = null

            if (buf.readVarInt() != 0) {
                val textureData = buf.readByteArray()
                val textureWidth = buf.readVarInt()
                val textureHeight = buf.readVarInt()
                val imageFormat = buf.readVarInt()
                val unkImageData = buf.readVarInt()

                val png = YSMClientMapper.toPng(textureData, imageFormat, textureWidth, textureHeight)
                iconTexture = OuterFileTexture(png)
            }

            var folderName = ""
            var folderDesc = ""
            val hasYSMPackInfo = buf.readVarInt()
            if (hasYSMPackInfo != 0) {
                folderName = buf.readString()
                folderDesc = buf.readString()
            }

            val languageData = HashMap<String, MutableMap<String, String>>()
            val languageSize = buf.readVarInt()
            for (j in 0 until languageSize) {
                val languageType = buf.readString()
                val translateKeySize = buf.readVarInt()
                val translationMap = HashMap<String, String>()
                for (k in 0 until translateKeySize) {
                    translationMap[buf.readString()] = buf.readString()
                }
                languageData[languageType] = translationMap
                val normalized = languageType.lowercase(Locale.ROOT).replace('-', '_')
                if (normalized != languageType) {
                    languageData[normalized] = translationMap
                }
            }
            parsedPacks.add(ModelPackData(folderPath, folderName, folderDesc, iconTexture, languageData))
        }

        if (parsedPacks.isNotEmpty()) {
            onModelPacksReceived(parsedPacks.toTypedArray())
        }

        val modelsToRemove = ArrayList<String>()
        for (loadedId in modelAssemblyMap.keys) {
            if ("default" == loadedId) continue

            when {
                !validServerModelIds.contains(loadedId) -> {
                    modelsToRemove.add(loadedId)
                }

                modelsToRequest.any { h ->
                    val serverCtx = serverModels[UUID(h.hash1, h.hash2)]
                    serverCtx != null && serverCtx.modelId == loadedId
                } -> {
                    modelsToRemove.add(loadedId)
                }
            }
        }

        if (modelsToRemove.isNotEmpty() || previousModelIds.isNotEmpty()) {
            val readyArr = BooleanArray(isModelReadyList.size) { isModelReadyList[it] }

            onModelContextsUpdated(
                if (modelsToRemove.isEmpty()) null else modelsToRemove.toTypedArray(),
                if (previousModelIds.isEmpty()) null else previousModelIds.toTypedArray(),
                if (updatedModelIds.isEmpty()) null else updatedModelIds.toTypedArray(),
                readyArr
            )
            Constants.LOGGER.info(
                "Cleaned up {} outdated models and updated {} existing models during sync.",
                modelsToRemove.size,
                previousModelIds.size
            )
        }

        syncStep = 3
        pendingModelsCount.set(modelsToRequest.size)

        val garbageLen = 16 + SECURE_RANDOM.nextInt(48)
        val garbage = ByteArray(garbageLen)
        SECURE_RANDOM.nextBytes(garbage)

        val currentKey1 = key1
        if (currentKey1 != null) {
            YSMByteBuf(Unpooled.buffer()).use { outBuf ->
                outBuf.writeGarbageHeader(garbageLen, garbage)
                outBuf.rawBuf.writeByte(0x04)

                outBuf.writeVarInt(modelsToRequest.size)
                for ((hash1, hash2) in modelsToRequest) {
                    outBuf.writeVarLong(hash1)
                    outBuf.writeVarLong(hash2)
                }

                val result = YsmCrypt.encrypt(outBuf.toArray(), currentKey1, false)
                sendModelFile(ByteBuffer.wrap(result.data()))
            }
        }

        if (pendingModelsCount.get() == 0) {
            modelParseScope.launch {
                Constants.LOGGER.info("All models loaded from local cache. Handshake complete!")
                onSyncComplete()
            }
        }
    }

    private fun handlePacket05(buf: YSMByteBuf) {
        buf.skipGarbageHeader()
        val type = buf.readVarInt()
        if (type != 5) return

        val hash1 = buf.readVarLong()
        val hash2 = buf.readVarLong()
        val uuid = UUID(hash1, hash2)

        val ctx = serverModels[uuid] ?: run {
            Constants.LOGGER.warn("Received unexpected file chunk for model: {}", uuid)
            return
        }

        val totalSize = buf.readVarInt()
        val chunkOffset = buf.readVarInt()
        val chunkLength = buf.readVarInt()

        var buffer = ctx.fileBuffer
        if (buffer == null) {
            buffer = ByteArray(totalSize)
            ctx.fileBuffer = buffer
            ctx.totalSize = totalSize
            ctx.bytesReceived = 0
        }

        buf.rawBuf.readBytes(buffer, chunkOffset, chunkLength)
        ctx.bytesReceived += chunkLength

        if (ctx.bytesReceived >= totalSize) {
            val fileBuffer = ctx.fileBuffer ?: return

            modelParseScope.launch {
                val currentClientKey = clientKey ?: return@launch
                val currentServerKey = serverKey ?: return@launch
                runCatching {
                    val folder = currentCacheFolderName ?: "default_cache"
                    val cacheDir = ServerModelManager.CACHE_CLIENT.resolve(folder).toFile()
                    if (!cacheDir.exists()) cacheDir.mkdirs()

                    val cachedFileData = YsmCrypt.transcodeServerDataToClientCache(
                        fileBuffer, currentServerKey, currentClientKey, hash1, hash2
                    )

                    val legitFileName = YSMClientCache.generateCacheFileName(hash1, hash2, currentClientKey)
                    @Suppress("ReplaceNotNullAssertionWithElvisReturn") val outFile = File(cacheDir, legitFileName!!)

                    FileOutputStream(outFile).use { it.write(cachedFileData) }

                    Constants.LOGGER.info("Downloaded & Cached: {}", outFile.absolutePath)
                    val decompressed = YsmCrypt.read(cachedFileData, currentClientKey)

                    parseAndLoadModel(decompressed, ctx.modelId, ctx.isAuth)
                }.onFailure {
                    if (it is CancellationException) return@onFailure
                    Constants.LOGGER.error("Failed to save/parse downloaded model: ${ctx.modelId}", it)
                }.also {
                    if (pendingModelsCount.decrementAndGet() <= 0) {
                        Constants.LOGGER.info("All missing models downloaded and loaded successfully!")
                        onSyncComplete()
                    }
                }
            }
        }
    }

    private fun parseAndLoadModel(decompressed: ByteArray, modelId: String, isAuth: Boolean) {
        runCatching {
            YSMBinaryDeserializer(decompressed, 32).use { deserializer ->
                val rawModel = deserializer.deserializeKeepOpen()
                val reader = deserializer.reader

                rawModel.footer.version = reader.readVarInt()
                rawModel.footer.unkInt1 = reader.readVarInt()
                if (rawModel.footer.unkInt1 != 0) {
                    rawModel.footer.rand = reader.readString()
                }

                rawModel.footer.time = reader.readVarLong()

                if (rawModel.footer.unkInt1 != 0) {
                    rawModel.footer.extra = reader.readString()
                    rawModel.footer.unkInt2 = reader.readVarInt()
                }

                val parsedBundle = YSMClientMapper.buildParsedBundle(rawModel, modelId)
                onModelDataReceived(parsedBundle, modelId, isPrimary = false, isAuth = isAuth)
            }
        }.onFailure { e ->
            Constants.LOGGER.error("Failed to parse and load model: $modelId", e)
        }
    }

    private fun toOrderedTextureMap(textures: Map<String, OuterFileTexture>?): OrderedStringMap<String, OuterFileTexture> {
        if (textures.isNullOrEmpty()) return OrderedStringMap(emptyArray(), emptyArray())
        return OrderedStringMap(
            textures.keys.toTypedArray(),
            textures.values.toTypedArray()
        )
    }

    private fun resetClientState() {
        syncStep = 1
        key1 = null
        lastKey = null
        serverKey = null
        clientKey = null

        modelParseJob.cancelChildren()

        currentCacheFolderName = null
        pendingModelsCount.set(0)
        cachedModelHashes.clear()

        serverModels.clear()

        val oldPreviews = modelPackMap
        if (oldPreviews.isNotEmpty()) {
            for ((path, _, _, texture) in oldPreviews.values) {
                if (texture != null) {
                    val loc = FileTypeUtil.getPackIconLocation(path)
                    Minecraft.getInstance().execute {
                        Minecraft.getInstance().textureManager.release(loc)
                    }
                }
            }
        }

        modelPackMap = Object2ReferenceOpenHashMap()
        _localModelContext = null
        _defaultTexture = null
        pendingModelCallback = null
        pendingModelQueue.clear()
        loadDefaultModel()

        forEachGuiWidget { widget ->
            runCatching {
                widget.onSyncBegin()
            }.onFailure {
                Constants.LOGGER.warn("Failed to sync widget", it)
            }
        }
    }

    val syncStatus: SyncStatus
        get() {
            RenderSystem.assertOnRenderThread()
            return syncState
        }

    fun getModelContext(str: String): ModelAssembly? = modelAssemblyMap[str]

    fun findModelContext(str: String): ModelAssembly? = modelAssemblyMap[str]

    val localModelContext: ModelAssembly
        get() {
            runPendingModelCallback()
            flushPendingModels()

            _localModelContext?.let { return it }

            loadDefaultModel()
            _localModelContext?.let { return it }

            val reg = modelAssemblyMap
            if (reg.isNotEmpty()) {
                var model = reg["default"]
                if (model == null) {
                    for (v in reg.values) {
                        model = v
                        break
                    }
                }
                if (model != null) {
                    _localModelContext = model
                    return model
                }
            }
            throw IllegalStateException("No default model context available")
        }

    val defaultTexture: Identifier
        get() = _defaultTexture?.getResourceLocation() ?: Identifier.parse("minecraft:missingno")

    fun <T : IGuiWidget> registerGuiWidget(widget: T): T {
        guiWidgets[widget] = null
        return widget
    }

    fun unregisterGuiWidget(guiWidget: IGuiWidget) {
        guiWidgets.remove(guiWidget)
    }

    private fun forEachGuiWidget(action: (IGuiWidget) -> Unit) {
        for (widget in guiWidgets.keys) {
            runCatching {
                action(widget)
            }.onFailure { th ->
                th.printStackTrace()
            }
        }
    }

    fun resetSync() {
        isOysmServer = false
        allowUpload = false
        processServerData(null)
        NetworkHandler.resetClientHandshake()
        Minecraft.getInstance().execute {
            syncState.setState(SyncState.WAITING)
        }
    }

    fun isAllowUpload(): Boolean = allowUpload

    fun isOysmServer(): Boolean = isOysmServer

    private fun sendModelFile(byteBuffer: ByteBuffer) {
        if (Minecraft.getInstance().player != null) {
            runCatching {
                NetworkHandler.sendToServer(C2SModelSyncPayload(byteBuffer))
            }.onFailure { e ->
                e.printStackTrace()
            }
            return
        }
        val connection = serverConnection ?: return
        if (!connection.isConnected) return
        runCatching {
            connection.send(NetworkHandler.toServerboundPacket(C2SModelSyncPayload(byteBuffer)))
        }.onFailure {
            Constants.LOGGER.error("Error during model file send", it)
        }
    }

    fun startSync(connection: Connection?, byteBuffer: ByteBuffer?) {
        serverConnection = connection
        processServerData(byteBuffer)
    }

    fun onSyncConnected() {
        if (Minecraft.getInstance().isLocalServer) {
            syncState.setState(SyncState.LOADING)
        } else {
            syncState.setState(SyncState.IDLE)
        }
        forEachGuiWidget { it.onSyncBegin() }
    }

    private fun onSyncProgress(totalModels: Int) {
        if (totalModels == -1) {
            Minecraft.getInstance().execute {
                syncState.setState(SyncState.PREPARING)
                forEachGuiWidget { it.onSyncError() }
            }
        } else {
            Minecraft.getInstance().execute {
                if (totalModels > 0) {
                    syncState.startSyncing(totalModels)
                } else {
                    syncState.setState(SyncState.IDLE)
                }
                forEachGuiWidget { guiWidget -> guiWidget.onSyncProgress(totalModels, 0) }
            }
        }
    }

    private fun onModelPacksReceived(packDataArr: Array<ModelPackData>) {
        val newPackMap = Object2ReferenceOpenHashMap<String, ModelPackData>()

        for (var1 in packDataArr) {
            var packData = var1
            if (StringUtils.isBlank(packData.name)) {
                packData = ModelPackData(
                    packData.path,
                    FileTypeUtil.getFinalPathSegment(packData.path),
                    packData.description,
                    packData.texture,
                    packData.translations
                )
            }
            newPackMap[packData.path] = packData
            val iconTexture = packData.texture
            if (iconTexture != null) {
                val location2 = FileTypeUtil.getPackIconLocation(packData.path)
                Minecraft.getInstance().submit {
                    Minecraft.getInstance().textureManager.register(location2, iconTexture)
                    iconTexture.load()
                }
            }
        }

        for ((path, _, _, texture) in modelPackMap.values) {
            if (!newPackMap.containsKey(path) && texture != null) {
                val location = FileTypeUtil.getPackIconLocation(path)
                Minecraft.getInstance().submit { Minecraft.getInstance().textureManager.release(location) }
            }
        }
        modelPackMap = newPackMap
    }

    private fun onModelContextsUpdated(
        removedModelIds: Array<String>?,
        previousModelIds: Array<String>?,
        updatedModelIds: Array<String>?,
        isModelReady: BooleanArray
    ) {
        Minecraft.getInstance().execute {
            val map = Object2ReferenceOpenHashMap(modelAssemblyMap)
            if (removedModelIds != null) {
                val removed = ArrayList<ModelAssembly>(removedModelIds.size)
                for (str in removedModelIds) {
                    val assembly = map.remove(str)
                    if (assembly != null) {
                        removed.add(assembly)
                    }
                }
                Minecraft.getInstance().execute {
                    for ((animationBundle, projectileModels, vehicleModels, _, _, _, textures) in removed) {
                        for (tex in textures) UploadManager.removeTexture(tex)
                        if (NativeLibLoader.isLoaded) {
                            for ((_, value) in projectileModels) value.model.freeNativeCache()
                            for ((_, value) in vehicleModels) value.model.freeNativeCache()
                            animationBundle.mainModel.freeNativeCache()
                            animationBundle.armModel.freeNativeCache()
                        }
                    }
                }
            }
            if (previousModelIds != null && updatedModelIds != null) {
                val modelAssemblies = arrayOfNulls<ModelAssembly>(previousModelIds.size)
                for (i in previousModelIds.indices) {
                    modelAssemblies[i] = map.remove(previousModelIds[i])
                }
                for (i in modelAssemblies.indices) {
                    val modelAssembly = modelAssemblies[i]
                    if (modelAssembly != null) {
                        modelAssembly.textureRegistry.isAuthModel = isModelReady[i]
                        map[updatedModelIds[i]] = modelAssembly
                    }
                }
            }
            modelAssemblyMap = map
            if (!removedModelIds.isNullOrEmpty() || !previousModelIds.isNullOrEmpty()) {
                forEachGuiWidget { guiWidget ->
                    guiWidget.onModelsLoaded(map)
                }
            }
        }
    }

    private fun onModelDataReceived(
        parsedBundle: ClientModelInfo?,
        modelId: String,
        isPrimary: Boolean,
        isAuth: Boolean
    ) {
        if (isPrimary) {
            pendingModelCallback = {
                processModelData(parsedBundle, modelId, isPrimary = true, isAuth = false)
            }
        } else {
            runPendingModelCallback()
            processModelData(parsedBundle, modelId, isPrimary = false, isAuth = isAuth)
        }
    }

    fun runPendingModelCallback() {
        val callback = pendingModelCallback ?: return
        pendingModelCallback = null
        callback()
    }

    private fun processModelData(parsedBundle: ClientModelInfo?, modelId: String, isPrimary: Boolean, isAuth: Boolean) {
        if (parsedBundle != null) {
            runCatching {
                val runtimeModel = ModelAssemblyFactory.buildAssembly(parsedBundle, isPrimary, isAuth)
                pendingModelQueue.add(Pair.of(runtimeModel, modelId))
                if (isPrimary) {
                    _localModelContext = runtimeModel

                    Minecraft.getInstance().execute {
                        val textures = runtimeModel.animationBundle.textures
                        if (!textures.isEmpty()) {
                            _defaultTexture = UploadManager.getOrCreateLocatable(textures.getValueAt(0), true)
                        }
                    }
                    return
                }
            }.onFailure {
                if (isPrimary) throw it
                Constants.LOGGER.error(StringFormattedMessage("Failed to process {}", modelId).formattedMessage, it)
                return
            }
        }
        Minecraft.getInstance().execute {
            if (syncState.currentState == SyncState.SYNCING) {
                syncState.syncedModels++
                val loaded = syncState.syncedModels
                if (loaded == syncState.totalModels) syncState.setState(SyncState.IDLE)
                forEachGuiWidget { guiWidget ->
                    guiWidget.onSyncProgress(syncState.totalModels, loaded)
                }
            }
        }
    }

    private fun onSyncComplete() {
        syncStep = 1
        serverModels.clear()
        cachedModelHashes.clear()

        Minecraft.getInstance().execute {
            syncState.setState(SyncState.IDLE)
            forEachGuiWidget { it.onSyncComplete() }
        }
    }

    fun setAllowUpload(allowUpload: Boolean) {
        ClientModelManager.allowUpload = allowUpload
    }

    fun setOysmServer(isOysmServer: Boolean) {
        ClientModelManager.isOysmServer = isOysmServer
    }

    private fun onSyncError(obj: Any?) {
        Minecraft.getInstance().execute {
            syncState.setState(SyncState.IDLE)
            forEachGuiWidget { guiWidget ->
                guiWidget.onSyncMessage(obj as? Component)
            }
            if (obj is Component) {
                Minecraft.getInstance().player?.displayClientMessage(obj, false)
                Constants.LOGGER.error(obj.getString(256))
            }
        }
    }

    fun flushPendingModels() {
        if (pendingModelQueue.isEmpty()) return
        val object2ReferenceOpenHashMap = Object2ReferenceOpenHashMap(modelAssemblyMap)
        while (true) {
            val pairPoll = pendingModelQueue.poll()
            if (pairPoll != null) {
                object2ReferenceOpenHashMap[pairPoll.right] = pairPoll.left
            } else {
                modelAssemblyMap = object2ReferenceOpenHashMap
                forEachGuiWidget { guiWidget -> guiWidget.onModelsUpdated(object2ReferenceOpenHashMap) }
                return
            }
        }
    }

    val pendingModelCount: Int
        get() = pendingModelQueue.size

    fun exportAllCachedModels(extra: String? = null, callback: ((ExportResult) -> Unit)?) {
        YSMThreadPool.launch {
            runCatching {
                val currentClientKey = clientKey
                if (currentClientKey == null) {
                    callback?.invoke(
                        ExportResult(
                            false,
                            Component.literal("未连接到服务器或尚未完成握手同步，无法获取客户端解密密钥。"),
                            "",
                            "",
                            0
                        )
                    )
                    return@launch
                }

                val folder = currentCacheFolderName ?: "default_cache"
                val cacheDir = ServerModelManager.CACHE_CLIENT.resolve(folder).toFile()

                if (!cacheDir.exists() || !cacheDir.isDirectory) {
                    callback?.invoke(
                        ExportResult(
                            false,
                            Component.literal("尚未生成任何缓存或缓存文件夹不存在: $folder"),
                            "",
                            "",
                            0
                        )
                    )
                    return@launch
                }

                val files = cacheDir.listFiles()
                if (files.isNullOrEmpty()) {
                    callback?.invoke(
                        ExportResult(
                            false,
                            Component.literal("缓存文件夹中没有任何模型可供导出。"),
                            "",
                            "",
                            0
                        )
                    )
                    return@launch
                }

                var successCount = 0
                for (file in files) {
                    if (!file.isFile) continue

                    runCatching {
                        val fileBytes = Files.readAllBytes(file.toPath())
                        val clearText = YsmCrypt.read(fileBytes, currentClientKey)

                        val coreDataLength: Int
                        var exportName = file.name

                        YSMBinaryDeserializer(clearText, 32).use { deserializer ->
                            val rawModel = deserializer.deserializeKeepOpen()
                            coreDataLength = deserializer.reader.rawBuf.readerIndex()

                            val metaName = rawModel.metadata.name
                            when {
                                metaName.isNotBlank() -> {
                                    exportName = metaName.trim()
                                }

                                else -> {
                                    val sha256 = rawModel.properties.sha256
                                    if (sha256.isNotEmpty()) {
                                        exportName = sha256
                                    }
                                }
                            }
                        }

                        exportName = exportName.replace(Regex("[\\\\/:*?\"<>|]"), "_")

                        YSMByteBuf(Unpooled.buffer()).use { outBuf ->
                            outBuf.writeDword(32)
                            outBuf.rawBuf.writeBytes(clearText, 0, coreDataLength)

                            outBuf.writeVarInt(32)
                            outBuf.writeVarInt(1)

                            val randBytes = ByteArray(8)
                            SECURE_RANDOM.nextBytes(randBytes)
                            val sb = StringBuilder(16)
                            for (b in randBytes) {
                                sb.append(String.format("%02x", b))
                            }
                            outBuf.writeString(sb.toString())

                            outBuf.writeVarLong(Instant.now().epochSecond)
                            outBuf.writeString(extra ?: "")
                            outBuf.writeVarInt(0)

                            val rawBytes = ByteArray(outBuf.rawBuf.readableBytes())
                            outBuf.rawBuf.readBytes(rawBytes)

                            val finalEncrypted = YsmCrypt.encryptYsmFile(rawBytes)
                            val exportPath = ServerModelManager.EXPORT.resolve("$exportName.ysm")
                            Files.createDirectories(exportPath.parent)
                            Files.write(exportPath, finalEncrypted)

                            successCount++
                            Constants.LOGGER.info("Successfully exported cached model to: {}", exportPath)
                        }
                    }.onFailure {
                        Constants.LOGGER.error("Failed to export cached model: " + file.name, it)
                    }
                }

                if (callback != null) {
                    val displayPath = Paths.get("export").toString()
                    if (successCount > 0) {
                        callback(ExportResult(true, null, displayPath, "", 0))
                    } else {
                        callback(
                            ExportResult(
                                false,
                                Component.literal("导出完成，但没有成功导出任何模型。可能是缓存已损坏。"),
                                "",
                                "",
                                0
                            )
                        )
                    }
                }
            }.onFailure {
                Constants.LOGGER.error("Error during batch export", it)
                callback?.invoke(
                    ExportResult(
                        false,
                        Component.literal("批量导出过程发生严重错误: " + it.message),
                        "",
                        "",
                        0
                    )
                )
            }
        }
    }
}
