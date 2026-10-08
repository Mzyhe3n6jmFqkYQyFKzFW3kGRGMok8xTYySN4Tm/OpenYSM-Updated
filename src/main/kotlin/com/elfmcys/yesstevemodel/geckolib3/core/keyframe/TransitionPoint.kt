package com.elfmcys.yesstevemodel.geckolib3.core.keyframe

import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationControllerContext
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.TransitionKeyFrame
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext
import com.elfmcys.yesstevemodel.geckolib3.core.util.MathUtil
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import org.joml.Vector3f

class TransitionPoint(
    currentTick: Float,
    val lerpFactor: Float,
    totalTick: Float,
    val offsetPoint: Vector3f,
    private val dstKeyframe: TransitionKeyFrame,
    context: AnimationControllerContext
) : AnimationPoint(currentTick, totalTick, context) {
    override fun getLerpPoint(evaluator: ExpressionEvaluator<AnimationContext<*>>): Vector3f {
        setupControllerContext(evaluator)
        val vector3f: Vector3f = dstKeyframe.evaluate(evaluator)
        MathUtil.lerpValues(lerpFactor, offsetPoint, vector3f, vector3f)
        val cached = cachedValue
        if (cached == null) {
            cachedValue = Vector3f(vector3f)
        } else {
            cached.set(vector3f)
        }
        return vector3f
    }

    fun evaluateRaw(evaluator: ExpressionEvaluator<AnimationContext<*>>): Vector3f {
        setupControllerContext(evaluator)
        return dstKeyframe.evaluate(evaluator)
    }
}