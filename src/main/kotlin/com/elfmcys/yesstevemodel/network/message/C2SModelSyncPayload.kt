package com.elfmcys.yesstevemodel.network.message

import com.elfmcys.yesstevemodel.model.ServerModelManager
import net.minecraft.network.FriendlyByteBuf
import rip.ysm.api.network.PacketContext
import java.nio.ByteBuffer

@JvmRecord
data class C2SModelSyncPayload(val data: ByteBuffer) {
    companion object {
        @JvmStatic
        fun encode(message: C2SModelSyncPayload, buf: FriendlyByteBuf) {
            buf.writeBytes(message.data)
        }

        @JvmStatic
        fun decode(buf: FriendlyByteBuf): C2SModelSyncPayload {
            val data = ByteBuffer.allocateDirect(buf.readableBytes())
            buf.readBytes(data)
            return C2SModelSyncPayload(data)
        }

        @JvmStatic
        fun handle(message: C2SModelSyncPayload, ctx: PacketContext) {
            val sender = ctx.sender
            if (ctx.isServerSide() && sender != null) {
                ServerModelManager.nativeSendModelData(sender.uuid, message.data)
            }
        }
    }
}