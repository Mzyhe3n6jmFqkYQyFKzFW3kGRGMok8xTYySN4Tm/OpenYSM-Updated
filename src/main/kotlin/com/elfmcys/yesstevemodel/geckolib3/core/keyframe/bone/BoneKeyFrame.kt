package com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone

import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import org.joml.Vector3f

abstract class BoneKeyFrame(
    val startTick: Float,
    val totalTick: Float,
    val beginPoint: Vector3v
) {
    val endTick: Float = startTick + totalTick

    abstract fun evaluate(evaluator: ExpressionEvaluator<*>, percentCompleted: Float): Vector3f

    companion object {
        fun isBegin(percentCompleted: Float): Boolean = percentCompleted < 0.00001f

        fun isEnd(percentCompleted: Float): Boolean = percentCompleted > 0.99999f
    }
}