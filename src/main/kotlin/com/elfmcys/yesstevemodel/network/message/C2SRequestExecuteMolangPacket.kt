package com.elfmcys.yesstevemodel.network.message

import com.elfmcys.yesstevemodel.network.NetworkHandler
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer
import rip.ysm.api.network.PacketContext

class C2SRequestExecuteMolangPacket(
    val animationName: String,
    val entityId: Int
) {
    companion object {
        fun encode(message: C2SRequestExecuteMolangPacket, buf: FriendlyByteBuf) {
            buf.writeUtf(message.animationName)
            buf.writeVarInt(message.entityId)
        }

        fun decode(buf: FriendlyByteBuf): C2SRequestExecuteMolangPacket {
            return C2SRequestExecuteMolangPacket(buf.readUtf(), buf.readVarInt())
        }

        fun handle(message: C2SRequestExecuteMolangPacket, ctx: PacketContext) {
            if (ctx.isServerSide()) {
                val sender = ctx.sender
                ctx.enqueueWork {
                    if (sender != null) {
                        handleOnServer(message, sender)
                    }
                }
            }
        }

        fun handleOnServer(message: C2SRequestExecuteMolangPacket, sender: ServerPlayer) {
            if (!sender.isAlive) {
                return
            }
            val entity = sender.level().getEntity(message.entityId) ?: return
            NetworkHandler.sendToTrackingEntity(S2CExecuteMolangPacket(message.entityId, message.animationName), entity)
        }
    }
}