package com.elfmcys.yesstevemodel.network.message

import net.minecraft.network.FriendlyByteBuf
import rip.ysm.api.network.PacketContext

@JvmRecord
data class C2SModelUploadChunkPacket(
    val uploadId: Long,
    val offset: Int,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as C2SModelUploadChunkPacket
        if (uploadId != other.uploadId) return false
        if (offset != other.offset) return false
        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var result = uploadId.hashCode()
        result = 31 * result + offset
        result = 31 * result + data.contentHashCode()
        return result
    }

    companion object {
        @JvmStatic
        fun encode(message: C2SModelUploadChunkPacket, buf: FriendlyByteBuf) {
            buf.writeVarLong(message.uploadId)
            buf.writeVarInt(message.offset)
            buf.writeByteArray(message.data)
        }

        @JvmStatic
        fun decode(buf: FriendlyByteBuf): C2SModelUploadChunkPacket {
            return C2SModelUploadChunkPacket(buf.readVarLong(), buf.readVarInt(), buf.readByteArray())
        }

        @JvmStatic
        fun handle(message: C2SModelUploadChunkPacket, ctx: PacketContext) {
        }
    }
}