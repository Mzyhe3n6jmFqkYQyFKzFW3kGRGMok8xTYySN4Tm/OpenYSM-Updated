package com.elfmcys.yesstevemodel.geckolib3.core.keyframe

import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationControllerContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import org.joml.Vector3f

abstract class AnimationPoint(
    @JvmField var currentTick: Float,
    @JvmField var totalTick: Float,
    var context: AnimationControllerContext
) {
    @JvmField var cachedValue: Vector3f? = null

    open fun getPercentCompleted(): Float {
        return if (totalTick == 0.0f) 1.0f else currentTick / totalTick
    }

    open fun setupControllerContext(evaluator: ExpressionEvaluator<AnimationContext<*>>) {
        evaluator.entity().setAnimationControllerContext(context)
    }

    abstract fun getLerpPoint(evaluator: ExpressionEvaluator<AnimationContext<*>>): Vector3f
}