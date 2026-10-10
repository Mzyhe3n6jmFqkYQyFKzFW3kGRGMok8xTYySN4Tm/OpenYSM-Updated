package com.elfmcys.yesstevemodel.client.animation.predicate

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.client.entity.GeckoVehicleEntity
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator

class RideStateAnimationPredicate : IAnimationPredicate<GeckoVehicleEntity> {
    override fun predicate(event: AnimationEvent<GeckoVehicleEntity>, evaluator: ExpressionEvaluator<*>?): PlayState {
        val entity = event.animatable.entity
        if (entity.passengers.isNotEmpty()) return IAnimationPredicate.predicate(event, "has_ride")
        return IAnimationPredicate.predicate(event, "not_ride")
    }

    companion object {
        val ANIMATION_NAMES: Array<String> = arrayOf("has_ride", "not_ride")
    }
}