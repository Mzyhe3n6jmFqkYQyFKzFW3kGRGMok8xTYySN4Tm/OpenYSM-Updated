package com.elfmcys.yesstevemodel.client.animation.predicate

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionPassenger
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity

class OffhandAttackAnimationPredicate : IAnimationPredicate<LivingAnimatable<*>> {
    override fun predicate(event: AnimationEvent<LivingAnimatable<*>>, evaluator: ExpressionEvaluator<*>?): PlayState {
        val animatable = event.getAnimatable()
        val livingEntity: LivingEntity = animatable.entity as? LivingEntity ?: return PlayState.STOP
        if (animatable is IPreviewAnimatable) {
            return PlayState.STOP
        }
        val firstPassenger: Entity? = livingEntity.firstPassenger
        if (firstPassenger == null || !firstPassenger.isAlive) {
            return PlayState.STOP
        }
        val conditionPassenger: ConditionPassenger = animatable.getModelConfig()?.passenger ?: return PlayState.STOP
        val str: String = conditionPassenger.doTest(livingEntity)
        if (str.isNotBlank()) {
            return IAnimationPredicate.playAnimationWithLoop(event, str, ILoopType.EDefaultLoopTypes.LOOP)
        }
        return PlayState.STOP
    }
}