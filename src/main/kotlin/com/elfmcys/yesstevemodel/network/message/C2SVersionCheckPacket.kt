package com.elfmcys.yesstevemodel.network.message

import com.elfmcys.yesstevemodel.capability.AuthModelsCapability
import com.elfmcys.yesstevemodel.capability.ModelInfoCapability
import com.elfmcys.yesstevemodel.capability.StarModelsCapability
import com.elfmcys.yesstevemodel.model.ServerModelManager
import com.elfmcys.yesstevemodel.network.NetworkHandler
import net.minecraft.network.FriendlyByteBuf
import rip.ysm.api.network.PacketContext

class C2SVersionCheckPacket(val version: String = NetworkHandler.VERSION) {
    companion object {
        @JvmStatic
        fun decode(buf: FriendlyByteBuf): C2SVersionCheckPacket {
            return C2SVersionCheckPacket(buf.readUtf())
        }

        @JvmStatic
        fun encode(message: C2SVersionCheckPacket, buf: FriendlyByteBuf) {
            buf.writeUtf(message.version)
        }

        @JvmStatic
        fun handle(message: C2SVersionCheckPacket, ctx: PacketContext) {
            val sender = ctx.sender
            if (sender != null && NetworkHandler.setChannelVersion(ctx.connection, message.version)) {
                ServerModelManager.validatePlayerModel(sender)
                ModelInfoCapability[sender]?.let { cap ->
                    cap.setMandatory(false)
                    cap.stopAnimation(sender)
                }
                AuthModelsCapability[sender]?.let { cap ->
                    NetworkHandler.sendToClientPlayer(S2CSyncAuthModelsPacket(cap.getAuthModels()), sender)
                }
                StarModelsCapability[sender]?.let { cap ->
                    NetworkHandler.sendToClientPlayer(S2CSyncStarModelsPacket(cap.getStarModels()), sender)
                }
                ServerModelManager.requestPlayerAuth(sender)
            }
        }
    }
}