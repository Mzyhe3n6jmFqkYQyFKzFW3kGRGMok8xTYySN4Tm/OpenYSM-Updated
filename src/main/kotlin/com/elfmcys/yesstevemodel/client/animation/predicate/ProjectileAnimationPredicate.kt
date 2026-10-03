package com.elfmcys.yesstevemodel.client.animation.predicate

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.client.entity.GeckoProjectileEntity
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import com.elfmcys.yesstevemodel.util.accessors.ProjectileStateAccessor
import net.minecraft.world.entity.projectile.Projectile

class ProjectileAnimationPredicate : IAnimationPredicate<GeckoProjectileEntity> {
    override fun predicate(event: AnimationEvent<GeckoProjectileEntity>, evaluator: ExpressionEvaluator<*>?): PlayState {
        val projectile: Projectile = event.getAnimatable().entity ?: return PlayState.STOP
        if (projectile.isInWater) {
            return IAnimationPredicate.predicate(event, "water")
        }
        if (projectile.isOnFire) {
            return IAnimationPredicate.predicate(event, "fire")
        }
        if (projectile is ProjectileStateAccessor && projectile.`ysm$isArrowInGround`()) {
            return IAnimationPredicate.predicate(event, "ground")
        }
        return IAnimationPredicate.predicate(event, "air")
    }

    companion object {
        @JvmField
        val ENVIRONMENT_STATES: Array<String> = arrayOf("water", "ground", "fly", "fire")
    }
}