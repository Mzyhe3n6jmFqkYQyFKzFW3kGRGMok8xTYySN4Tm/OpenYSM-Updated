package com.elfmcys.yesstevemodel.network.message

import com.elfmcys.yesstevemodel.capability.StarModelsCapability
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer
import rip.ysm.api.network.PacketContext

class C2SSetStarModelPacket(
    val modelId: String,
    val isAdd: Boolean
) {
    companion object {
        @JvmStatic
        fun add(modelId: String): C2SSetStarModelPacket {
            return C2SSetStarModelPacket(modelId, true)
        }

        @JvmStatic
        fun remove(modelId: String): C2SSetStarModelPacket {
            return C2SSetStarModelPacket(modelId, false)
        }

        @JvmStatic
        fun encode(message: C2SSetStarModelPacket, buf: FriendlyByteBuf) {
            buf.writeUtf(message.modelId)
            buf.writeBoolean(message.isAdd)
        }

        @JvmStatic
        fun decode(buf: FriendlyByteBuf): C2SSetStarModelPacket {
            return C2SSetStarModelPacket(buf.readUtf(), buf.readBoolean())
        }

        @JvmStatic
        fun handle(message: C2SSetStarModelPacket, ctx: PacketContext) {
            if (ctx.isServerSide()) {
                val sender = ctx.sender
                ctx.enqueueWork {
                    if (sender != null) {
                        handleCapability(message, sender)
                    }
                }
            }
        }

        @JvmStatic
        fun handleCapability(message: C2SSetStarModelPacket, sender: ServerPlayer) {
            StarModelsCapability[sender]?.let { cap ->
                if (message.isAdd) {
                    cap.addModel(message.modelId)
                } else {
                    cap.removeModel(message.modelId)
                }
            }
        }
    }
}