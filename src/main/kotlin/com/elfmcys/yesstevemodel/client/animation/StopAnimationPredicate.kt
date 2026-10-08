package com.elfmcys.yesstevemodel.client.animation

import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator

object StopAnimationPredicate : IAnimationPredicate<AnimatableEntity<*>> {
    override fun predicate(
        event: AnimationEvent<AnimatableEntity<*>>,
        evaluator: ExpressionEvaluator<*>?
    ): PlayState = PlayState.STOP
}