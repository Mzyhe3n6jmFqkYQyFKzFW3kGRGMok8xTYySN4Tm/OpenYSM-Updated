package com.elfmcys.yesstevemodel.client.animation.predicate

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator

class NamedAnimationPredicate<T : AnimatableEntity<*>>(private val animationName: String) : IAnimationPredicate<T> {
    override fun predicate(event: AnimationEvent<T>, evaluator: ExpressionEvaluator<*>?): PlayState {
        return IAnimationPredicate.playLoopAnimation(event, animationName)
    }
}