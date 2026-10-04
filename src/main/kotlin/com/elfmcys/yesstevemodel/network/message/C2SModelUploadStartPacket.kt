package com.elfmcys.yesstevemodel.network.message

import net.minecraft.network.FriendlyByteBuf
import rip.ysm.api.network.PacketContext

data class C2SModelUploadStartPacket(
    val modelId: String,
    val totalBytes: Int,
    val sha256: String
) {
    companion object {
        @JvmStatic
        fun encode(message: C2SModelUploadStartPacket, buf: FriendlyByteBuf) {
            buf.writeUtf(message.modelId)
            buf.writeVarInt(message.totalBytes)
            buf.writeUtf(message.sha256)
        }

        @JvmStatic
        fun decode(buf: FriendlyByteBuf): C2SModelUploadStartPacket {
            return C2SModelUploadStartPacket(buf.readUtf(), buf.readVarInt(), buf.readUtf())
        }

        @JvmStatic
        fun handle(message: C2SModelUploadStartPacket, ctx: PacketContext) {
        }
    }
}