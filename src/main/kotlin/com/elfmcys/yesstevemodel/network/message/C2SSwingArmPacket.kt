package com.elfmcys.yesstevemodel.network.message

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.game.ClientboundAnimatePacket
import net.minecraft.server.level.ServerChunkCache
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.effect.MobEffectUtil
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.LivingEntity
import rip.ysm.api.item.ToolActionBridge
import rip.ysm.api.network.PacketContext

class C2SSwingArmPacket(val hand: InteractionHand) {
    companion object {
        @JvmStatic
        fun encode(message: C2SSwingArmPacket, buf: FriendlyByteBuf) {
            buf.writeEnum(message.hand)
        }

        @JvmStatic
        fun decode(buf: FriendlyByteBuf): C2SSwingArmPacket {
            return C2SSwingArmPacket(buf.readEnum(InteractionHand::class.java))
        }

        @JvmStatic
        fun handle(message: C2SSwingArmPacket, ctx: PacketContext) {
            val sender = ctx.sender
            if (ctx.isServerSide() && sender != null) {
                ctx.enqueueWork {
                    processSwingArm(message, sender)
                }
            }
        }

        @JvmStatic
        fun processSwingArm(message: C2SSwingArmPacket, sender: ServerPlayer) {
            val interactionHand = message.hand
            val itemInHand = sender.getItemInHand(interactionHand)
            if (itemInHand.isEmpty || !ToolActionBridge.onEntitySwing(itemInHand, sender)) {
                if (!sender.swinging || sender.swingTime >= getSwingDuration(sender) / 2 || sender.swingTime < 0) {
                    sender.swingTime = -1
                    sender.swinging = true
                    sender.swingingArm = interactionHand
                    if (sender.level() is ServerLevel) {
                        (sender.level().chunkSource as ServerChunkCache).sendToTrackingPlayersAndSelf(
                            sender,
                            ClientboundAnimatePacket(sender, if (interactionHand == InteractionHand.MAIN_HAND) 0 else 3)
                        )
                    }
                }
            }
        }

        @JvmStatic
        fun getSwingDuration(entity: LivingEntity): Int {
            if (MobEffectUtil.hasDigSpeed(entity)) {
                return 6 - 1 + MobEffectUtil.getDigSpeedAmplification(entity)
            }
            if (entity.hasEffect(MobEffects.MINING_FATIGUE)) {
                val effect = entity.getEffect(MobEffects.MINING_FATIGUE)
                val amplifier = effect?.amplifier ?: 0
                return 6 + 1 + amplifier * 2
            }
            return 6
        }
    }
}