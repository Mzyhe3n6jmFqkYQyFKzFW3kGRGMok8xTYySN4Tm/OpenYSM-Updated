package com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone

import com.elfmcys.yesstevemodel.geckolib3.core.util.MathUtil
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import org.joml.Vector3f

open class LinearKeyFrame(
    startTick: Float,
    totalTick: Float,
    beginPoint: Vector3v,
    val endPoint: Vector3v,
    val postPoint: Vector3v
) : BoneKeyFrame(startTick, totalTick, beginPoint) {
    override fun evaluate(evaluator: ExpressionEvaluator<*>, percentCompleted: Float): Vector3f {
        if (isBegin(percentCompleted)) {
            return beginPoint.eval(evaluator)
        }
        if (isEnd(percentCompleted)) {
            return postPoint.eval(evaluator)
        }
        return MathUtil.lerpValues(percentCompleted, beginPoint.eval(evaluator), endPoint.eval(evaluator))
    }
}