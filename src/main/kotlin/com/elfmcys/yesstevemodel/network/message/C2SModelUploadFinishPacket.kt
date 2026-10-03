package com.elfmcys.yesstevemodel.network.message

import net.minecraft.network.FriendlyByteBuf
import rip.ysm.api.network.PacketContext

@JvmRecord
data class C2SModelUploadFinishPacket(val uploadId: Long) {
    companion object {
        @JvmStatic
        fun encode(message: C2SModelUploadFinishPacket, buf: FriendlyByteBuf) {
            buf.writeVarLong(message.uploadId)
        }

        @JvmStatic
        fun decode(buf: FriendlyByteBuf): C2SModelUploadFinishPacket {
            return C2SModelUploadFinishPacket(buf.readVarLong())
        }

        @JvmStatic
        fun handle(message: C2SModelUploadFinishPacket, ctx: PacketContext) {
        }
    }
}