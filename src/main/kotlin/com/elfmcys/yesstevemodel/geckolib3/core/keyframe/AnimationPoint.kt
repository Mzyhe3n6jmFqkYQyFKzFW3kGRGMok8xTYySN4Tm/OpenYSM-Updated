package com.elfmcys.yesstevemodel.geckolib3.core.keyframe

import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationControllerContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import org.joml.Vector3f

abstract class AnimationPoint(
    val currentTick: Float,
    val totalTick: Float,
    val context: AnimationControllerContext
) {
    var cachedValue: Vector3f? = null
        protected set

    open val percentCompleted: Float
        get() = if (totalTick == 0.0f) 1.0f else currentTick / totalTick

    open fun setupControllerContext(evaluator: ExpressionEvaluator<AnimationContext<*>>) =
        evaluator.entity().setAnimationControllerContext(context)

    abstract fun getLerpPoint(evaluator: ExpressionEvaluator<AnimationContext<*>>): Vector3f
}