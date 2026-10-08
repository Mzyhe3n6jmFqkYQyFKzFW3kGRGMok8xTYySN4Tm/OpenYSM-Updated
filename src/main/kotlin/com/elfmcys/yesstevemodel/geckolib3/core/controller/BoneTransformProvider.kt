package com.elfmcys.yesstevemodel.geckolib3.core.controller

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneTopLevelSnapshot
import com.elfmcys.yesstevemodel.geckolib3.core.util.TransitionVector3f
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator

interface BoneTransformProvider {
    val boneTarget: BoneTopLevelSnapshot
    fun getRotation(evaluator: ExpressionEvaluator<AnimationContext<*>>): TransitionVector3f?
    fun getPosition(evaluator: ExpressionEvaluator<AnimationContext<*>>): TransitionVector3f?
    fun getScale(evaluator: ExpressionEvaluator<AnimationContext<*>>): TransitionVector3f?
}