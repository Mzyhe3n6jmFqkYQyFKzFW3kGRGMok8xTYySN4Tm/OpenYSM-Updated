package com.elfmcys.yesstevemodel.network.message

import com.elfmcys.yesstevemodel.capability.PlayerCapability
import it.unimi.dsi.fastutil.floats.FloatArrayList
import net.minecraft.client.Minecraft
import net.minecraft.network.FriendlyByteBuf
import rip.ysm.api.network.PacketContext

class S2CSyncAnimationExpressionPacket(
    val entityId: Int,
    val floatData: FloatArrayList
) {
    companion object {
        fun encode(message: S2CSyncAnimationExpressionPacket, buf: FriendlyByteBuf) {
            buf.writeVarInt(message.entityId)
            buf.writeByte(message.floatData.size)
            for (i in message.floatData.indices) {
                buf.writeFloat(message.floatData.getFloat(i))
            }
        }

        fun decode(buf: FriendlyByteBuf): S2CSyncAnimationExpressionPacket {
            val entityId = buf.readVarInt()
            val count = buf.readByte().toInt()
            val floatArrayList = FloatArrayList(count)
            repeat(count) {
                floatArrayList.add(buf.readFloat())
            }
            return S2CSyncAnimationExpressionPacket(entityId, floatArrayList)
        }

        fun handleCapability(message: S2CSyncAnimationExpressionPacket, ctx: PacketContext) {
            if (ctx.isClientSide()) {
                ctx.enqueueWork {
                    val level = Minecraft.getInstance().level ?: return@enqueueWork
                    val entity = level.getEntity(message.entityId) ?: return@enqueueWork
                    PlayerCapability[entity]?.executeAnimationExpression(message.floatData)
                }
            }
        }
    }
}