package com.elfmcys.yesstevemodel.client.animation.predicate

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.client.entity.GeckoVehicleEntity
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator

class EntityMovementPredicate : IAnimationPredicate<GeckoVehicleEntity> {
    override fun predicate(event: AnimationEvent<GeckoVehicleEntity>, evaluator: ExpressionEvaluator<*>?): PlayState {
        val entity = event.animatable.entity
        if (entity.isInWater) return IAnimationPredicate.predicate(event, "water")
        if (entity.onGround()) return IAnimationPredicate.predicate(event, "ground")
        return IAnimationPredicate.predicate(event, "fly")
    }

    companion object {
        @JvmField
        val MOVEMENT_STATES: Array<String> = arrayOf("water", "ground", "fly")
    }
}