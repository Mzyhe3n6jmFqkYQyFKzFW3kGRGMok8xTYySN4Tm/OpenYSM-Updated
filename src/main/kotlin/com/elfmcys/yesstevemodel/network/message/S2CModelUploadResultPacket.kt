package com.elfmcys.yesstevemodel.network.message

import com.elfmcys.yesstevemodel.client.upload.ModelUploadSession
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.network.FriendlyByteBuf
import rip.ysm.api.network.PacketContext

data class S2CModelUploadResultPacket(
    val uploadId: Long,
    val status: Byte,
    val modelId: String,
    val h1: Long,
    val h2: Long,
    val message: String
) {
    companion object {
        @JvmStatic
        fun encode(packet: S2CModelUploadResultPacket, buf: FriendlyByteBuf) {
            buf.writeVarLong(packet.uploadId)
            buf.writeByte(packet.status.toInt())
            buf.writeUtf(packet.modelId)
            buf.writeVarLong(packet.h1)
            buf.writeVarLong(packet.h2)
            buf.writeUtf(packet.message)
        }

        @JvmStatic
        fun decode(buf: FriendlyByteBuf): S2CModelUploadResultPacket {
            return S2CModelUploadResultPacket(
                buf.readVarLong(),
                buf.readByte(),
                buf.readUtf(),
                buf.readVarLong(),
                buf.readVarLong(),
                buf.readUtf()
            )
        }

        @JvmStatic
        fun handle(packet: S2CModelUploadResultPacket, ctx: PacketContext) {
            if (ctx.isClientSide()) {
                ctx.enqueueWork {
                    handleOnClient(packet)
                }
            }
        }

        @JvmStatic
        @Environment(EnvType.CLIENT)
        fun handleOnClient(packet: S2CModelUploadResultPacket) {
            ModelUploadSession.onResult(
                packet.uploadId,
                packet.status,
                packet.modelId,
                packet.h1,
                packet.h2,
                packet.message
            )
        }
    }
}