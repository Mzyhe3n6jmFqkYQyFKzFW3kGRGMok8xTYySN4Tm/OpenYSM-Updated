package com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone

import com.elfmcys.yesstevemodel.geckolib3.core.util.MathUtil
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import org.joml.Vector3f

class CatmullRomKeyFrame(
    startTick: Float,
    totalTick: Float,
    private val leftPoint: Vector3v,
    current: Vector3v,
    private val endPoint: Vector3v,
    private val rightPoint: Vector3v,
    private val postPoint: Vector3v
) : BoneKeyFrame(startTick, totalTick, current) {
    override fun evaluate(evaluator: ExpressionEvaluator<*>, percentCompleted: Float): Vector3f {
        if (isBegin(percentCompleted)) return beginPoint.eval(evaluator)
        if (isEnd(percentCompleted)) return postPoint.eval(evaluator)
        return MathUtil.catmullRom(
            percentCompleted,
            leftPoint.eval(evaluator),
            beginPoint.eval(evaluator),
            endPoint.eval(evaluator),
            rightPoint.eval(evaluator)
        )
    }
}