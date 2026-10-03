package com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone

import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import org.joml.Vector3f

abstract class BoneKeyFrame(
    @JvmField val startTick: Float,
    @JvmField val totalTick: Float,
    @JvmField val beginPoint: Vector3v
) {
    @JvmField val endTick: Float = startTick + totalTick

    abstract fun evaluate(evaluator: ExpressionEvaluator<*>, percentCompleted: Float): Vector3f
    open fun getStartTick(): Float = startTick
    open fun getTotalTick(): Float = totalTick
    open fun getEndTick(): Float = endTick

    companion object {
        @JvmStatic fun isBegin(percentCompleted: Float): Boolean = percentCompleted < 0.00001f
        @JvmStatic fun isEnd(percentCompleted: Float): Boolean = percentCompleted > 0.99999f
    }
}