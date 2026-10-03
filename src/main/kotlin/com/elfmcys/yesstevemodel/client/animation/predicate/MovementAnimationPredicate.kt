package com.elfmcys.yesstevemodel.client.animation.predicate

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.client.entity.GeckoVehicleEntity
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import kotlin.math.sqrt

class MovementAnimationPredicate : IAnimationPredicate<GeckoVehicleEntity> {
    override fun predicate(event: AnimationEvent<GeckoVehicleEntity>, evaluator: ExpressionEvaluator<*>?): PlayState {
        val entity: Entity = event.getAnimatable().entity ?: return PlayState.STOP
        val deltaMovement: Vec3 = entity.deltaMovement
        if (sqrt(deltaMovement.x * deltaMovement.x + deltaMovement.z * deltaMovement.z) > 0.05) {
            return IAnimationPredicate.predicate(event, "forward")
        }
        return IAnimationPredicate.predicate(event, "idle")
    }

    companion object {
        @JvmField
        val ANIMATION_NAMES: Array<String> = arrayOf("forward", "idle")
    }
}