@file:Suppress("unused")

package com.elfmcys.yesstevemodel.client.animation.predicate

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import rip.ysm.compat.parcool.ParcoolCompat

class PlayerSpecialAnimationPredicate : IAnimationPredicate<CustomPlayerEntity> {
    override fun predicate(event: AnimationEvent<CustomPlayerEntity>, evaluator: ExpressionEvaluator<*>?): PlayState {
        val animatable = event.animatable
        val player = animatable.entity
        if (animatable is IPreviewAnimatable) return PlayState.STOP
        val str = ParcoolCompat.getActionName(player)
        if (animatable.getAnimation(str) != null) return IAnimationPredicate.predicate(event, str)
        return PlayState.STOP
    }
}