package com.elfmcys.yesstevemodel.network.message

import com.elfmcys.yesstevemodel.capability.StarModelsCapability
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.Minecraft
import net.minecraft.network.FriendlyByteBuf
import rip.ysm.api.network.PacketContext

class S2CSyncStarModelsPacket(val starModels: MutableSet<String>) {
    companion object {
        @JvmStatic
        fun encode(message: S2CSyncStarModelsPacket, buf: FriendlyByteBuf) {
            buf.writeVarInt(message.starModels.size)
            for (starModel in message.starModels) {
                buf.writeUtf(starModel)
            }
        }

        @JvmStatic
        fun decode(buf: FriendlyByteBuf): S2CSyncStarModelsPacket {
            val varInt = buf.readVarInt()
            val tmp = HashSet<String>(varInt)
            for (i in 0 until varInt) {
                tmp.add(buf.readUtf())
            }
            return S2CSyncStarModelsPacket(tmp)
        }

        @JvmStatic
        fun handle(message: S2CSyncStarModelsPacket, ctx: PacketContext) {
            if (ctx.isClientSide()) {
                ctx.enqueueWork {
                    handleCapability(message)
                }
            }
        }

        @JvmStatic
        @Environment(EnvType.CLIENT)
        fun handleCapability(message: S2CSyncStarModelsPacket) {
            val player = Minecraft.getInstance().player ?: return
            StarModelsCapability[player]?.setStarModels(message.starModels)
        }
    }
}