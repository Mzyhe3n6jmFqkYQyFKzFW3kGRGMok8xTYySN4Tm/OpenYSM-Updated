package com.elfmcys.yesstevemodel.client.animation.predicate

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator

class PlayerBaseAnimationPredicate : IAnimationPredicate<CustomPlayerEntity> {
    override fun predicate(event: AnimationEvent<CustomPlayerEntity>, evaluator: ExpressionEvaluator<*>?): PlayState {
        val playerEntity: CustomPlayerEntity = event.animatable
        if (playerEntity is IPreviewAnimatable) {
            val tracker = playerEntity.animationStateMachine
            if (tracker.hasAnimation) {
                return IAnimationPredicate.playLoopAnimation(event, tracker.currentAnimation)
            }
            return PlayState.STOP
        }
        if (playerEntity.isModelSwitching) {
            if (playerEntity.isDisabledState) {
                playerEntity.enableModel()
                event.controller?.stopTransition()
            }
            return IAnimationPredicate.predicate(event, playerEntity.selectedModelId)
        }
        return PlayState.STOP
    }
}