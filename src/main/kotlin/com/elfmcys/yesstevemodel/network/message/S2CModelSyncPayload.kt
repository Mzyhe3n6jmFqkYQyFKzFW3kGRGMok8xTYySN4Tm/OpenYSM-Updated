package com.elfmcys.yesstevemodel.network.message

import com.elfmcys.yesstevemodel.client.ClientModelManager
import net.minecraft.network.FriendlyByteBuf
import rip.ysm.api.network.PacketContext
import java.nio.ByteBuffer

@JvmRecord
data class S2CModelSyncPayload(val data: ByteBuffer) {
    companion object {
        @JvmStatic
        fun encode(message: S2CModelSyncPayload, buf: FriendlyByteBuf) {
            buf.writeBytes(message.data)
        }

        @JvmStatic
        fun decode(buf: FriendlyByteBuf): S2CModelSyncPayload {
            val data = ByteBuffer.allocateDirect(buf.readableBytes())
            buf.readBytes(data)
            return S2CModelSyncPayload(data)
        }

        @JvmStatic
        fun handle(message: S2CModelSyncPayload, ctx: PacketContext) {
            if (ctx.isClientSide()) {
                ClientModelManager.startSync(ctx.connection, message.data)
            }
        }
    }
}