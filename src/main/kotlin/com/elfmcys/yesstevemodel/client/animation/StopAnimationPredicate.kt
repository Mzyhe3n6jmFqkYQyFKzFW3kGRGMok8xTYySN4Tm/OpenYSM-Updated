package com.elfmcys.yesstevemodel.client.animation

import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import net.minecraft.world.entity.LivingEntity

class StopAnimationPredicate : IAnimationPredicate<AnimatableEntity<LivingEntity>> {
    override fun predicate(
        event: AnimationEvent<AnimatableEntity<LivingEntity>>,
        evaluator: ExpressionEvaluator<*>?
    ): PlayState =
        PlayState.STOP

    companion object {
        @JvmField
        val INSTANCE: StopAnimationPredicate = StopAnimationPredicate()
    }
}