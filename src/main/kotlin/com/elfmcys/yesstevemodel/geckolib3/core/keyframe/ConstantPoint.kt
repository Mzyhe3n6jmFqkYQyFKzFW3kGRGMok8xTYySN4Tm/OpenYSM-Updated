package com.elfmcys.yesstevemodel.geckolib3.core.keyframe

import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationControllerContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import org.joml.Vector3f

open class ConstantPoint(
    currentTick: Float,
    totalTick: Float,
    @JvmField val value: Vector3f,
    context: AnimationControllerContext
) : AnimationPoint(currentTick, totalTick, context) {
    override val percentCompleted: Float
        get() {
            if (totalTick == 0.0f) return if (currentTick == 0.0f) 0.0f else 1.0f
            return currentTick / totalTick
        }

    override fun getLerpPoint(evaluator: ExpressionEvaluator<AnimationContext<*>>): Vector3f {
        val cached = cachedValue
        if (cached == null) {
            cachedValue = Vector3f(value)
        } else {
            cached.set(value)
        }
        return value
    }
}