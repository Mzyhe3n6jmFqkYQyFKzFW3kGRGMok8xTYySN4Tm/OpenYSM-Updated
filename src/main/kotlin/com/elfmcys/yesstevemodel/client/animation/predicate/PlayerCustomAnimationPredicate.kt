package com.elfmcys.yesstevemodel.client.animation.predicate

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator

class PlayerCustomAnimationPredicate : IAnimationPredicate<CustomPlayerEntity> {
    override fun predicate(event: AnimationEvent<CustomPlayerEntity>, evaluator: ExpressionEvaluator<*>?): PlayState {
        val previewAnimatable: IPreviewAnimatable = event.getAnimatable() as? IPreviewAnimatable ?: return PlayState.STOP
        val str: String = previewAnimatable.getAnimationStateMachine().previousAnimation
        if (str.isNotBlank()) {
            previewAnimatable.setCustomAnimationActive(true)
            return IAnimationPredicate.playLoopAnimation(event, str)
        }
        previewAnimatable.setCustomAnimationActive(false)
        event.getController()?.markDirty()
        return PlayState.STOP
    }
}