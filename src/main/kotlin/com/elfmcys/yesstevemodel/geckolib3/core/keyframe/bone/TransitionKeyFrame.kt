package com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone

import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import org.joml.Vector3f

open class TransitionKeyFrame(
    firstStartTick: Float,
    firstPoint: Vector3v,
    val postPoint: Vector3v
) : BoneKeyFrame(0.0f, firstStartTick, firstPoint) {
    override fun evaluate(evaluator: ExpressionEvaluator<*>, percentCompleted: Float): Vector3f {
        if (!isEnd(percentCompleted)) {
            return beginPoint.eval(evaluator)
        }
        return postPoint.eval(evaluator)
    }

    open fun evaluate(evaluator: ExpressionEvaluator<*>): Vector3f {
        return beginPoint.eval(evaluator)
    }
}