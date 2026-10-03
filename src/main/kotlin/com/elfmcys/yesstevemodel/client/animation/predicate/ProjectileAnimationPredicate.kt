package com.elfmcys.yesstevemodel.client.animation.predicate

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.client.entity.GeckoProjectileEntity
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import com.elfmcys.yesstevemodel.util.accessors.ProjectileStateAccessor

class ProjectileAnimationPredicate : IAnimationPredicate<GeckoProjectileEntity> {
    override fun predicate(
        event: AnimationEvent<GeckoProjectileEntity>,
        evaluator: ExpressionEvaluator<*>?
    ): PlayState {
        val projectile = event.getAnimatable().entity
        return when {
            projectile.isInWater -> IAnimationPredicate.predicate(event, "water")
            projectile.isOnFire -> IAnimationPredicate.predicate(event, "fire")
            projectile is ProjectileStateAccessor && projectile.`ysm$isArrowInGround`() -> IAnimationPredicate.predicate(
                event,
                "ground"
            )

            else -> IAnimationPredicate.predicate(event, "air")
        }
    }

    companion object {
        @JvmField
        val ENVIRONMENT_STATES: Array<String> = arrayOf("water", "ground", "fly", "fire")
    }
}