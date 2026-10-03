package com.elfmcys.yesstevemodel.geckolib3.core.controller

import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneTopLevelSnapshot
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap

interface IAnimationController<T : AnimatableEntity<*>> {
    fun getName(): String
    fun getCurrentAnimation(): String
    fun init(
        list: MutableList<BoneTopLevelSnapshot>,
        object2ReferenceMap: Object2ReferenceMap<String, MutableList<IValue>>
    )

    fun process(event: AnimationEvent<T>, evaluator: ExpressionEvaluator<AnimationContext<*>>, isSomething: Boolean)
    fun forEachTransform(consumer: (BoneTransformProvider) -> Unit)
    fun reset()
    fun isDeprecatedMode(): Boolean = false
}