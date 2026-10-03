package com.elfmcys.yesstevemodel.client.animation.predicate

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import net.minecraft.world.InteractionHand
import rip.ysm.compat.ironsspellbooks.SpellbooksCompat
import rip.ysm.compat.slashblade.SlashBladeCompat

class ItemHoldAnimationPredicate : IAnimationPredicate<LivingAnimatable<*>> {
    override fun predicate(event: AnimationEvent<LivingAnimatable<*>>, evaluator: ExpressionEvaluator<*>?): PlayState {
        val animatable = event.getAnimatable()
        val livingEntity = animatable.entity
        if (animatable is IPreviewAnimatable) return PlayState.STOP
        val playState = SpellbooksCompat.resolvePlayState(event, livingEntity)
        if (playState != null) return playState
        val i = animatable.getModelAssembly()?.modelData?.formatVersion ?: 0
        if (!livingEntity.isSleeping && SlashBladeCompat.isSlashBladeItem(livingEntity.getItemInHand(InteractionHand.MAIN_HAND))) {
            if (event.getController()?.isPlaying() == true) event.getController()?.stopTransition()
            val str = SlashBladeCompat.getComboAnimName(event)
            if (str.isNotBlank()) {
                if (animatable.getAnimation(str) != null) {
                    return IAnimationPredicate.playAnimationWithValid(
                        event,
                        str,
                        ILoopType.EDefaultLoopTypes.PLAY_ONCE,
                        i
                    )
                }
                return PlayState.CONTINUE
            }
        }
        if (livingEntity.swinging && !livingEntity.isSleeping) {
            if (livingEntity.swingTime == 0 && animatable.getPositionTracker().markProcessed(1))
                event.getController()?.stopTransition()
            val conditionManager = animatable.getModelConfig() ?: return PlayState.CONTINUE
            val conditionSwing =
                if (livingEntity.swingingArm == InteractionHand.MAIN_HAND) conditionManager.swingMainhand else conditionManager.swingOffhand
            val str2 = conditionSwing.doTest(livingEntity, livingEntity.swingingArm)
            if (str2.isNotBlank())
                return IAnimationPredicate.playAnimationWithValid(event, str2, ILoopType.EDefaultLoopTypes.PLAY_ONCE, i)
            return IAnimationPredicate.playAnimationWithValid(
                event,
                if (livingEntity.swingingArm == InteractionHand.MAIN_HAND) "swing_hand" else "swing_offhand",
                ILoopType.EDefaultLoopTypes.PLAY_ONCE,
                i
            )
        }
        return PlayState.CONTINUE
    }
}