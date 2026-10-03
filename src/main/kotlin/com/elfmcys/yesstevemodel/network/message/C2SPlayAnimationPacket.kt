package com.elfmcys.yesstevemodel.network.message

import com.elfmcys.yesstevemodel.capability.ModelInfoCapability
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.model.ServerModelManager
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer
import org.apache.commons.lang3.StringUtils
import rip.ysm.api.network.PacketContext
import rip.ysm.compat.touhoulittlemaid.TouhouMaidCompat

class C2SPlayAnimationPacket(
    val animationIndex: Int,
    val category: String,
    val entityId: Int = -1
) {
    companion object {
        @JvmStatic
        fun createDefault(): C2SPlayAnimationPacket {
            return C2SPlayAnimationPacket(-1, StringPool.EMPTY)
        }

        @JvmStatic
        fun createWithIndex(entityId: Int): C2SPlayAnimationPacket {
            return C2SPlayAnimationPacket(-1, StringPool.EMPTY, entityId)
        }

        @JvmStatic
        fun encode(message: C2SPlayAnimationPacket, buf: FriendlyByteBuf) {
            buf.writeVarInt(message.animationIndex)
            buf.writeUtf(message.category)
            buf.writeVarInt(message.entityId)
        }

        @JvmStatic
        fun decode(buf: FriendlyByteBuf): C2SPlayAnimationPacket {
            return C2SPlayAnimationPacket(buf.readVarInt(), buf.readUtf(), buf.readVarInt())
        }

        @JvmStatic
        fun handle(message: C2SPlayAnimationPacket, ctx: PacketContext) {
            if (ctx.isServerSide()) {
                ctx.enqueueWork {
                    val sender = ctx.sender ?: return@enqueueWork
                    handleCapability(message, sender)
                }
            }
        }

        @JvmStatic
        fun handleCapability(message: C2SPlayAnimationPacket, sender: ServerPlayer) {
            if (message.entityId != -1) {
                val entity = sender.level().getEntity(message.entityId) ?: return
                if (TouhouMaidCompat.isMaidEntity(entity)) {
                    TouhouMaidCompat.registerAnimationRoulette(entity, message.category, message.animationIndex)
                }
                return
            }
            ModelInfoCapability[sender]?.let { modelInfoCap ->
                if (message.animationIndex == -1) {
                    modelInfoCap.stopAnimation(sender)
                } else {
                    ServerModelManager[modelInfoCap.getModelId()]?.let { serverModelCap ->
                        val modelProperties = serverModelCap.getLoadedModelData().modelProperties
                        val extraAnimationClassify = modelProperties.extraAnimationClassify
                        val extraAnimations =
                            if (StringUtils.isNotBlank(message.category) && extraAnimationClassify.containsKey(message.category)) {
                                extraAnimationClassify[message.category] ?: return@let
                            } else {
                                modelProperties.extraAnimation
                            }
                        if (extraAnimations.size > message.animationIndex) {
                            modelInfoCap.playAnimation(sender, extraAnimations.getKeyAt(message.animationIndex))
                        }
                    }
                }
            }
        }
    }
}