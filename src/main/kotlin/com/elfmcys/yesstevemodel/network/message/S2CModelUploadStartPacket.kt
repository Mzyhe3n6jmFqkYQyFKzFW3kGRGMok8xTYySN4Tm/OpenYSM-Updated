package com.elfmcys.yesstevemodel.network.message

import com.elfmcys.yesstevemodel.client.upload.ModelUploadSession
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.network.FriendlyByteBuf
import rip.ysm.api.network.PacketContext

data class S2CModelUploadStartPacket(
    val uploadId: Long,
    val status: Byte,
    val chunkSize: Int,
    val maxTotalBytes: Int,
    val chunksPerTick: Int,
    val message: String
) {
    companion object {
        @JvmStatic
        fun encode(packet: S2CModelUploadStartPacket, buf: FriendlyByteBuf) {
            buf.writeVarLong(packet.uploadId)
            buf.writeByte(packet.status.toInt())
            buf.writeVarInt(packet.chunkSize)
            buf.writeVarInt(packet.maxTotalBytes)
            buf.writeVarInt(packet.chunksPerTick)
            buf.writeUtf(packet.message)
        }

        @JvmStatic
        fun decode(buf: FriendlyByteBuf): S2CModelUploadStartPacket {
            val uploadId = buf.readVarLong()
            val status = buf.readByte()
            val chunkSize = buf.readVarInt()
            val maxTotalBytes = buf.readVarInt()
            val chunksPerTick = buf.readVarInt()
            val message = buf.readUtf()
            return S2CModelUploadStartPacket(uploadId, status, chunkSize, maxTotalBytes, chunksPerTick, message)
        }

        @JvmStatic
        fun handle(packet: S2CModelUploadStartPacket, ctx: PacketContext) {
            if (ctx.isClientSide()) {
                ctx.enqueueWork {
                    handleOnClient(packet)
                }
            }
        }

        @JvmStatic
        @Environment(EnvType.CLIENT)
        fun handleOnClient(packet: S2CModelUploadStartPacket) {
            ModelUploadSession.onStartAck(
                packet.uploadId,
                packet.status,
                packet.chunkSize,
                packet.maxTotalBytes,
                packet.chunksPerTick,
                packet.message
            )
        }
    }
}