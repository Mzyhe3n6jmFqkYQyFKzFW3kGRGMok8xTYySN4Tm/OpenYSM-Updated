package com.elfmcys.yesstevemodel.client.animation.predicate

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.player.Player
import rip.ysm.compat.carryon.CarryOnDataHelper

class PlayerAnimationPredicate : IAnimationPredicate<CustomPlayerEntity> {
    override fun predicate(event: AnimationEvent<CustomPlayerEntity>, evaluator: ExpressionEvaluator<*>?): PlayState {
        val player: Player = event.animatable.entity
        return when {
            event.animatable is IPreviewAnimatable -> PlayState.STOP
            player.pose == Pose.SWIMMING -> PlayState.STOP
            player.pose == Pose.FALL_FLYING && player.isFallFlying -> PlayState.STOP
            CarryOnDataHelper.isPrincess(player) -> IAnimationPredicate.playLoopAnimation(event, "carryon:princess")
            else -> when (CarryOnDataHelper.getCarryType(player)) {
                CarryOnDataHelper.CarryType.ENTITY -> IAnimationPredicate.playLoopAnimation(event, "carryon:entity")
                CarryOnDataHelper.CarryType.BLOCK -> IAnimationPredicate.playLoopAnimation(event, "carryon:block")
                CarryOnDataHelper.CarryType.PLAYER -> IAnimationPredicate.playLoopAnimation(event, "carryon:player")
                else -> PlayState.STOP
            }
        }
    }
}