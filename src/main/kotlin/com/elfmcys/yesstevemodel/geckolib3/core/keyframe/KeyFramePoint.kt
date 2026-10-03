package com.elfmcys.yesstevemodel.geckolib3.core.keyframe

import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationControllerContext
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.BoneKeyFrame
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import org.joml.Vector3f

open class KeyFramePoint(
    currentTick: Float,
    @JvmField val keyFrame: BoneKeyFrame,
    context: AnimationControllerContext
) : AnimationPoint(currentTick, keyFrame.getTotalTick(), context) {
    override fun getLerpPoint(evaluator: ExpressionEvaluator<AnimationContext<*>>): Vector3f {
        setupControllerContext(evaluator)
        val vector3f: Vector3f = keyFrame.evaluate(evaluator, getPercentCompleted())
        val cached = cachedValue
        if (cached == null) {
            cachedValue = Vector3f(vector3f)
        } else {
            cached.set(vector3f)
        }
        return vector3f
    }
}