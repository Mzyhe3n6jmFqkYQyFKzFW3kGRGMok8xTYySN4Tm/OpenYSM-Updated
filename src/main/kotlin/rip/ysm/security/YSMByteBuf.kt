@file:Suppress("unused")

package rip.ysm.security

import io.netty.buffer.ByteBuf
import java.nio.charset.StandardCharsets

class YSMByteBuf(private val buf: ByteBuf) : AutoCloseable {
    val rawBuf: ByteBuf
        get() = buf

    fun skipGarbageHeader(): Int {
        val garbageLen = buf.readByte().toInt() and 0x7F
        buf.skipBytes(1)
        buf.skipBytes(garbageLen)
        return garbageLen
    }

    fun writeGarbageHeader(garbageLen: Int, garbageData: ByteArray) {
        buf.writeByte(garbageLen or 0x80)
        buf.writeByte(0x00)
        buf.writeBytes(garbageData)
    }

    var offset: Int
        get() = buf.readerIndex()
        set(value) {
            buf.readerIndex(value)
        }

    fun readByte(): Byte = buf.readByte()

    fun readFloat(): Float = buf.readFloatLE()

    fun readDword(): Long = buf.readUnsignedIntLE()

    fun writeDword(format: Int) {
        buf.writeIntLE(format)
    }

    fun readVarInt(): Int {
        var value = 0
        var position = 0
        while (true) {
            val currentByte = buf.readByte().toInt()
            value = value or (currentByte and 0x7F shl position)
            if (currentByte and 0x80 == 0) break
            position += 7
            if (position >= 64) throw RuntimeException("VarInt too big")
        }
        return value
    }

    fun writeVarInt(value: Int) {
        var v = value
        while (v and -128 != 0) {
            buf.writeByte(v and 127 or 128)
            v = v ushr 7
        }
        buf.writeByte(v)
    }

    fun readVarLong(): Long {
        var value = 0L
        var position = 0
        while (true) {
            val currentByte = buf.readByte().toInt()
            value = value or ((currentByte and 0x7F).toLong() shl position)
            if (currentByte and 0x80 == 0) {
                break
            }
            position += 7
            if (position >= 64) {
                throw RuntimeException("VarLong too big")
            }
        }
        return value
    }

    fun writeVarLong(value: Long) {
        var v = value
        while (v and -128L != 0L) {
            buf.writeByte((v and 127L).toInt() or 128)
            v = v ushr 7
        }
        buf.writeByte(v.toInt())
    }

    fun readByteArray(): ByteArray {
        val len = readVarInt()
        if (len == 0) return ByteArray(0)
        val bytes = ByteArray(len)
        buf.readBytes(bytes)
        return bytes
    }

    fun readString(): String {
        val len = readVarInt()
        if (len == 0) return ""
        val bytes = ByteArray(len)
        buf.readBytes(bytes)
        return String(bytes, StandardCharsets.UTF_8)
    }

    fun toArray(): ByteArray {
        val bytes = ByteArray(buf.readableBytes())
        buf.readBytes(bytes)
        return bytes
    }

    fun skipBytes(n: Int) {
        buf.skipBytes(n)
    }

    fun writeString(s: String?) {
        if (s.isNullOrEmpty()) {
            writeVarInt(0)
            return
        }
        val bytes = s.toByteArray(StandardCharsets.UTF_8)
        writeVarInt(bytes.size)
        buf.writeBytes(bytes)
    }

    fun writeByte(value: Byte) {
        buf.writeByte(value.toInt())
    }

    fun writeFloat(value: Float) {
        buf.writeFloatLE(value)
    }

    fun writeByteArray(data: ByteArray?) {
        if (data == null || data.isEmpty()) {
            writeVarInt(0)
            return
        }
        writeVarInt(data.size)
        buf.writeBytes(data)
    }

    fun writeByteBuf(other: ByteBuf) {
        buf.writeBytes(other)
    }

    fun release() {
        if (buf.refCnt() > 0) {
            buf.release()
        }
    }

    override fun close() = release()
}