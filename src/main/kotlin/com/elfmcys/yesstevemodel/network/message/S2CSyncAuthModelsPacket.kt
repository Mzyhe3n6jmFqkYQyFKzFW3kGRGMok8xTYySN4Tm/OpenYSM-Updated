package com.elfmcys.yesstevemodel.network.message

import com.elfmcys.yesstevemodel.client.ClientModelManager
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.network.FriendlyByteBuf
import rip.ysm.api.network.PacketContext

class S2CSyncAuthModelsPacket(val authModels: MutableSet<String>) {
    companion object {
        @JvmStatic
        fun encode(message: S2CSyncAuthModelsPacket, buf: FriendlyByteBuf) {
            buf.writeVarInt(message.authModels.size)
            for (modelId in message.authModels) buf.writeUtf(modelId)
        }

        @JvmStatic
        fun decode(buf: FriendlyByteBuf): S2CSyncAuthModelsPacket {
            val size = buf.readVarInt()
            val tmp = HashSet<String>(size)
            for (i in 0 until size) tmp.add(buf.readUtf())
            return S2CSyncAuthModelsPacket(tmp)
        }

        @JvmStatic
        fun handle(message: S2CSyncAuthModelsPacket, ctx: PacketContext) {
            if (!ctx.isClientSide()) return
            ctx.enqueueWork {
                handleCapability(message)
            }
        }

        @JvmStatic
        @Environment(EnvType.CLIENT)
        fun handleCapability(message: S2CSyncAuthModelsPacket) {
            ClientModelManager.authModels = message.authModels.toSet()
        }
    }
}