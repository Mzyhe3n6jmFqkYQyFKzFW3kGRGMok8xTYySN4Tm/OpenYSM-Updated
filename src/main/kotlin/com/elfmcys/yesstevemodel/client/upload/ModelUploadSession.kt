package com.elfmcys.yesstevemodel.client.upload

import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.network.message.C2SModelUploadChunkPacket
import com.elfmcys.yesstevemodel.network.message.C2SModelUploadFinishPacket
import com.elfmcys.yesstevemodel.network.message.C2SModelUploadStartPacket
import com.elfmcys.yesstevemodel.util.DigestUtil
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max
import kotlin.math.min

class ModelUploadSession(
    val modelId: String,
    private val data: ByteArray
) {
    val sha256: String = DigestUtil.sha256Hex(data)

    @Volatile
    var state: State = State.STARTING
        private set

    @Volatile
    var uploadId: Long = 0L
        private set

    @Volatile
    var chunkSize: Int = 32000
        private set

    @Volatile
    var chunksPerTick: Int = 4
        private set

    @Volatile
    private var nextOffset: Int = 0

    @Volatile
    var message: String = ""
        private set

    @Synchronized
    fun tick() {
        if (state != State.UPLOADING) {
            return
        }
        val budget = max(1, chunksPerTick)
        var i = 0
        while (i < budget && nextOffset < data.size) {
            val end = min(nextOffset + chunkSize, data.size)
            val slice = data.copyOfRange(nextOffset, end)
            NetworkHandler.sendToServer(C2SModelUploadChunkPacket(uploadId, nextOffset, slice))
            nextOffset = end
            i++
        }
        if (nextOffset >= data.size) {
            state = State.FINISHING
            message = "Verifying…"
            NetworkHandler.sendToServer(C2SModelUploadFinishPacket(uploadId))
        }
        notifyListeners()
    }

    private fun fail(reason: String) {
        state = State.FAILED
        message = reason
    }

    fun isTerminal(): Boolean {
        return state == State.COMPLETED || state == State.FAILED
    }

    fun getTotalBytes(): Int {
        return data.size
    }

    fun getSentBytes(): Int {
        return min(nextOffset, data.size)
    }

    fun getProgress(): Float {
        if (data.isEmpty() || state == State.COMPLETED) {
            return 1f
        }
        return getSentBytes().toFloat() / data.size
    }

    enum class State {
        STARTING,
        UPLOADING,
        FINISHING,
        COMPLETED,
        FAILED
    }

    fun interface Listener {
        fun onSessionUpdate(session: ModelUploadSession?)
    }

    companion object {
        private val listeners = CopyOnWriteArrayList<Listener>()

        @Volatile
        @JvmStatic
        var instance: ModelUploadSession? = null
            private set

        @Volatile
        private var serverLimitsKnown: Boolean = false

        @Volatile
        @JvmStatic
        var lastMaxTotalBytes: Int = 16777216
            private set

        @Volatile
        @JvmStatic
        var lastChunksPerTick: Int = 4
            private set

        @JvmStatic
        fun getInstance(): ModelUploadSession? {
            return instance
        }

        @Synchronized
        @JvmStatic
        fun start(modelId: String, data: ByteArray): String? {
            val currentInstance = instance
            if (currentInstance != null && !currentInstance.isTerminal()) {
                return "Upload already in progress"
            }
            if (data.isEmpty()) {
                return "Empty file"
            }
            if (serverLimitsKnown && data.size > lastMaxTotalBytes) {
                return "File exceeds server limit (${formatBytes(lastMaxTotalBytes)})"
            }
            if (!isYsmFile(data)) {
                return "Invalid file type!"
            }
            val session = ModelUploadSession(modelId, data)
            instance = session
            notifyListeners()
            NetworkHandler.sendToServer(C2SModelUploadStartPacket(modelId, data.size, session.sha256))
            return null
        }

        @JvmStatic
        fun hasServerLimits(): Boolean {
            return serverLimitsKnown
        }

        @JvmStatic
        fun formatBytes(bytes: Int): String {
            if (bytes < 1024) {
                return "$bytes B"
            }
            if (bytes < 1024 * 1024) {
                return String.format("%.1f KB", bytes / 1024.0)
            }
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0))
        }

        @Synchronized
        @JvmStatic
        fun clearIfTerminal() {
            val currentInstance = instance
            if (currentInstance != null && currentInstance.isTerminal()) {
                instance = null
                notifyListeners()
            }
        }

        @JvmStatic
        fun addListener(l: Listener) {
            listeners.add(l)
        }

        @JvmStatic
        fun removeListener(l: Listener) {
            listeners.remove(l)
        }

        @Synchronized
        @JvmStatic
        fun onStartAck(
            uploadId: Long,
            status: Byte,
            chunkSize: Int,
            maxTotalBytes: Int,
            chunksPerTick: Int,
            message: String
        ) {
            if (maxTotalBytes > 0) {
                lastMaxTotalBytes = maxTotalBytes
            }
            if (chunksPerTick > 0) {
                lastChunksPerTick = chunksPerTick
            }
            serverLimitsKnown = true
            val s = instance
            if (s == null || s.state != State.STARTING) {
                return
            }
            if (status != 0.toByte()) {
                s.fail(getRequestErrorText(status) + if (message.isEmpty()) "" else ": $message")
                return
            }
            s.uploadId = uploadId
            s.chunkSize = max(1, chunkSize)
            s.chunksPerTick = max(1, chunksPerTick)
            s.state = State.UPLOADING
            s.message = "Uploading…"
            notifyListeners()
        }

        @Synchronized
        @JvmStatic
        fun onResult(
            uploadId: Long,
            status: Byte,
            modelId: String,
            h1: Long,
            h2: Long,
            message: String
        ) {
            val s = instance
            if (s == null || s.uploadId != uploadId) {
                return
            }
            if (status == 0.toByte()) {
                s.state = State.COMPLETED
                s.message = "Uploaded as $modelId"
            } else {
                s.fail(getResponseErrorText(status) + if (message.isEmpty()) "" else ": $message")
            }
            notifyListeners()
        }

        @JvmStatic
        fun tickCurrent() {
            val s = instance ?: return
            s.tick()
        }

        @JvmStatic
        private fun notifyListeners() {
            val s = instance
            for (l in listeners) {
                l.onSessionUpdate(s)
            }
        }

        @JvmStatic
        private fun isYsmFile(data: ByteArray): Boolean {
            val ysmHeader = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte(), 0x59, 0x53, 0x47, 0x50)
            if (data.size < ysmHeader.size) {
                return false
            }
            for (i in ysmHeader.indices) {
                if (data[i] != ysmHeader[i]) {
                    return false
                }
            }
            return true
        }

        @JvmStatic
        private fun getRequestErrorText(status: Byte): String {
            return when (status.toInt()) {
                1 -> "Model ID already exists"
                2 -> "File exceeds server limit"
                3 -> "No upload permission"
                4 -> "Server busy, try again later"
                5 -> "Invalid model ID or hash"
                6 -> "Uploads disabled on server"
                else -> "error: $status"
            }
        }

        @JvmStatic
        private fun getResponseErrorText(status: Byte): String {
            return when (status.toInt()) {
                1 -> "Hash mismatch"
                2 -> "Server failed to parse model"
                3 -> "Server storage error"
                4 -> "Session expired"
                5 -> "Incomplete upload"
                6 -> "Server rejected write"
                else -> "error: $status"
            }
        }
    }
}