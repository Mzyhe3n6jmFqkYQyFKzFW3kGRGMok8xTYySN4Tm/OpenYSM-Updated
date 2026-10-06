package com.elfmcys.yesstevemodel.network.message

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.Minecraft
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.player.Player
import rip.ysm.api.network.PacketContext
import rip.ysm.compat.touhoulittlemaid.TouhouMaidCompat

class S2CExecuteMolangPacket {
    val entityIds: IntArray
    val expression: String

    constructor(entityId: Int, expression: String) {
        this.entityIds = intArrayOf(entityId)
        this.expression = expression
    }

    constructor(entityIds: IntArray, expression: String) {
        this.entityIds = entityIds
        this.expression = expression
    }

    companion object {
        @JvmStatic
        fun encode(message: S2CExecuteMolangPacket, buf: FriendlyByteBuf) {
            buf.writeVarIntArray(message.entityIds)
            buf.writeUtf(message.expression)
        }

        @JvmStatic
        fun decode(buf: FriendlyByteBuf): S2CExecuteMolangPacket {
            return S2CExecuteMolangPacket(buf.readVarIntArray(), buf.readUtf())
        }

        @JvmStatic
        fun handle(message: S2CExecuteMolangPacket, ctx: PacketContext) {
            if (ctx.isClientSide()) {
                ctx.enqueueWork {
                    handleCapability(message)
                }
            }
        }

        @JvmStatic
        @Environment(EnvType.CLIENT)
        fun handleCapability(message: S2CExecuteMolangPacket) {
            val level = Minecraft.getInstance().level ?: return
            for (i in message.entityIds) {
                val entity = level.getEntity(i) ?: continue
                when {
                    entity is Player -> {
                        PlayerCapability[entity]?.let { cap ->
                            runCatching {
                                cap.executeExpression(
                                    GeckoLibCache.parseSimpleExpression(message.expression),
                                    true,
                                    false
                                ) {}
                            }.onFailure { e ->
                                Constants.LOGGER.error("Failed to execute molang {}", message.expression, e)
                            }
                        }
                    }

                    TouhouMaidCompat.isMaidEntity(entity) -> {
                        TouhouMaidCompat.playMaidAnimation(entity, message.expression)
                    }
                }
            }
        }
    }
}