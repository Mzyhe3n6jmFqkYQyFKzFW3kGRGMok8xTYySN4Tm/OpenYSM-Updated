package com.elfmcys.yesstevemodel.client.animation.predicate

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionManager
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionUse
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity

class InteractionHandAnimationPredicate : IAnimationPredicate<LivingAnimatable<*>> {
    override fun predicate(event: AnimationEvent<LivingAnimatable<*>>, evaluator: ExpressionEvaluator<*>?): PlayState {
        val animatable = event.getAnimatable()
        val livingEntity: LivingEntity = animatable.entity as? LivingEntity ?: return PlayState.STOP
        if (animatable is IPreviewAnimatable) {
            return PlayState.STOP
        }
        val i: Int = animatable.getModelAssembly()?.modelData?.formatVersion ?: 0
        if (livingEntity.isUsingItem && !livingEntity.isSleeping) {
            if (livingEntity.ticksUsingItem == 1 && animatable.getPositionTracker().markProcessed(2)) {
                event.getController()?.stopTransition()
            }
            val conditionManager: ConditionManager = animatable.getModelConfig() ?: return PlayState.STOP
            if (livingEntity.usedItemHand == InteractionHand.MAIN_HAND) {
                val conditionUse: ConditionUse = conditionManager.useMainhand
                val str: String = conditionUse.doTest(livingEntity, InteractionHand.MAIN_HAND)
                if (str.isNotBlank()) {
                    return IAnimationPredicate.playAnimationWithValid(event, str, ILoopType.EDefaultLoopTypes.LOOP, i)
                }
                return IAnimationPredicate.playAnimationWithValid(event, "use_mainhand", ILoopType.EDefaultLoopTypes.LOOP, i)
            }
            val conditionUse2: ConditionUse = conditionManager.useOffhand
            val str2: String = conditionUse2.doTest(livingEntity, InteractionHand.OFF_HAND)
            if (str2.isNotBlank()) {
                return IAnimationPredicate.playAnimationWithValid(event, str2, ILoopType.EDefaultLoopTypes.LOOP, i)
            }
            return IAnimationPredicate.playAnimationWithValid(event, "use_offhand", ILoopType.EDefaultLoopTypes.LOOP, i)
        }
        return PlayState.STOP
    }
}