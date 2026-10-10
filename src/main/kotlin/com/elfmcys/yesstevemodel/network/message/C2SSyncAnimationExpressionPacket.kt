package com.elfmcys.yesstevemodel.network.message

import com.elfmcys.yesstevemodel.network.NetworkHandler
import it.unimi.dsi.fastutil.floats.FloatArrayList
import net.minecraft.network.FriendlyByteBuf
import rip.ysm.api.network.PacketContext

class C2SSyncAnimationExpressionPacket(val floatData: FloatArrayList) {
    companion object {
        fun encode(message: C2SSyncAnimationExpressionPacket, buf: FriendlyByteBuf) {
            buf.writeByte(message.floatData.size)
            for (i in message.floatData.indices) {
                buf.writeFloat(message.floatData.getFloat(i))
            }
        }

        fun decode(buf: FriendlyByteBuf): C2SSyncAnimationExpressionPacket {
            val size = buf.readByte().toInt()
            val floatArrayList = FloatArrayList(size)
            repeat(size) {
                floatArrayList.add(buf.readFloat())
            }
            return C2SSyncAnimationExpressionPacket(floatArrayList)
        }

        fun handle(message: C2SSyncAnimationExpressionPacket, ctx: PacketContext) {
            val sender = ctx.sender
            if (ctx.isServerSide() && sender != null) {
                ctx.enqueueWork {
                    NetworkHandler.sendToTrackingEntityAndSelf(
                        S2CSyncAnimationExpressionPacket(sender.id, message.floatData),
                        sender
                    )
                }
            }
        }
    }
}