package com.elfmcys.yesstevemodel.client.animation

import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator

fun interface IAnimationPredicate<T : AnimatableEntity<*>> {
    fun predicate(event: AnimationEvent<T>, evaluator: ExpressionEvaluator<*>?): PlayState

    companion object {
        @JvmStatic
        fun <T : AnimatableEntity<*>> playAnimationWithLoop(
            event: AnimationEvent<T>,
            animationName: String,
            loopType: ILoopType
        ): PlayState {
            event.controller?.setAnimation(animationName, loopType)
            return PlayState.CONTINUE
        }

        @JvmStatic
        fun <P : AnimatableEntity<*>> predicate(
            event: AnimationEvent<P>,
            animationName: String
        ): PlayState {
            event.controller?.setAnimation(animationName)
            return PlayState.CONTINUE
        }

        @JvmStatic
        fun <P : AnimatableEntity<*>> playAnimationWithValid(
            event: AnimationEvent<P>,
            animationName: String,
            loopType: ILoopType,
            version: Int
        ): PlayState {
            if (AnimationFormatValidator.validate(event, animationName, version))
                event.controller?.setAnimation(animationName) else event.controller?.setAnimation(
                animationName,
                loopType
            )
            return PlayState.CONTINUE
        }

        @JvmStatic
        fun <T : AnimatableEntity<*>> playLoopAnimation(
            event: AnimationEvent<T>,
            str: String
        ): PlayState = playAnimationWithLoop(event, str, ILoopType.EDefaultLoopTypes.LOOP)
    }
}