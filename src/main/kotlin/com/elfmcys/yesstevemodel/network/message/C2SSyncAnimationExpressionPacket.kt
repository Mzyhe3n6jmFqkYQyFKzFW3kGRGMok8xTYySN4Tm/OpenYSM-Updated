package com.elfmcys.yesstevemodel.network.message

import com.elfmcys.yesstevemodel.network.NetworkHandler
import it.unimi.dsi.fastutil.floats.FloatArrayList
import net.minecraft.network.FriendlyByteBuf
import rip.ysm.api.network.PacketContext

class C2SSyncAnimationExpressionPacket(val floatData: FloatArrayList) {
    companion object {
        @JvmStatic
        fun encode(message: C2SSyncAnimationExpressionPacket, buf: FriendlyByteBuf) {
            buf.writeByte(message.floatData.size)
            // TODO: 'fun next(): Float!' is deprecated. Deprecated in Java.
            for (floatDatum in message.floatData) {
                buf.writeFloat(floatDatum)
            }
        }

        @JvmStatic
        fun decode(buf: FriendlyByteBuf): C2SSyncAnimationExpressionPacket {
            val size = buf.readByte().toInt()
            val floatArrayList = FloatArrayList(size)
            for (i in 0 until size) {
                floatArrayList.add(buf.readFloat())
            }
            return C2SSyncAnimationExpressionPacket(floatArrayList)
        }

        @JvmStatic
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