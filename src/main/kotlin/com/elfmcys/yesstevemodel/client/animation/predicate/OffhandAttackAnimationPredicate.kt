package com.elfmcys.yesstevemodel.client.animation.predicate

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator

class OffhandAttackAnimationPredicate : IAnimationPredicate<LivingAnimatable<*>> {
    override fun predicate(event: AnimationEvent<LivingAnimatable<*>>, evaluator: ExpressionEvaluator<*>?): PlayState {
        val animatable = event.animatable
        val livingEntity = animatable.entity
        if (animatable is IPreviewAnimatable) return PlayState.STOP
        val firstPassenger = livingEntity.firstPassenger
        if (firstPassenger == null || !firstPassenger.isAlive) return PlayState.STOP
        val conditionPassenger = animatable.modelConfig?.passenger ?: return PlayState.STOP
        val str = conditionPassenger.doTest(livingEntity)
        if (str.isNotBlank())
            return IAnimationPredicate.playAnimationWithLoop(event, str, ILoopType.EDefaultLoopTypes.LOOP)
        return PlayState.STOP
    }
}