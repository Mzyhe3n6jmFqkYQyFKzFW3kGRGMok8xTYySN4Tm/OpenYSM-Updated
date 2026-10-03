package com.elfmcys.yesstevemodel.geckolib3.core.controller

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneTopLevelSnapshot
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap

open class CompositeAnimationController<T : AnimatableEntity<*>>(
    private val animatable: T,
    private val name: String,
    transitionLengthTicks: Float,
    predicate: IAnimationPredicate<*>,
    deprecatedMode: Boolean = false
) : IAnimationController<T> {
    private val controller: PredicateBasedController<T> =
        PredicateBasedController(animatable, name, transitionLengthTicks, predicate, deprecatedMode)
    private val animationRuntime: AnimationControllerRuntime<T> =
        AnimationControllerRuntime(animatable, name, transitionLengthTicks)
    private var initialized: Boolean = false
    private var activeController: IAnimationController<T> = controller

    constructor(animatable: T, name: String, transitionLengthTicks: Float, predicate: IAnimationPredicate<*>) : this(
        animatable,
        name,
        transitionLengthTicks,
        predicate
    )

    override fun getName(): String = name

    override fun getCurrentAnimation(): String {
        if (initialized) {
            return if (animationRuntime.isBuiltinAnimation()) "[builtin] " + controller.getCurrentAnimation() else animationRuntime.getCurrentAnimation()
        }
        return controller.getCurrentAnimation()
    }

    override fun init(
        list: MutableList<BoneTopLevelSnapshot>,
        object2ReferenceMap: Object2ReferenceMap<String, MutableList<IValue>>
    ) {
        val ctrl = animatable.getAnimationEntries(name)
        if (ctrl != null) {
            initialized = true
            animationRuntime.initWithBones(list, ctrl)
            controller.init(list, object2ReferenceMap)
            activeController = animationRuntime
            return
        }
        initialized = false
        controller.init(list, object2ReferenceMap)
        animationRuntime.reset()
        activeController = controller
    }

    override fun process(
        event: AnimationEvent<T>,
        evaluator: ExpressionEvaluator<AnimationContext<*>>,
        isFirstPerson: Boolean
    ) {
        if (initialized) {
            animationRuntime.process(event, evaluator, isFirstPerson)
            if (animationRuntime.isBuiltinAnimation()) {
                if (activeController != controller) {
                    animationRuntime.getCurrentEntry()?.blendTransition?.asInterpolator()?.let {
                        controller.setInterpolator(it)
                    }
                    activeController = controller
                }
                controller.process(event, evaluator, isFirstPerson)
                return
            }
            if (activeController != animationRuntime) {
                controller.evaluateExpressions(evaluator)
                controller.clearAnimation()
                activeController = animationRuntime
                return
            }
            return
        }
        controller.process(event, evaluator, isFirstPerson)
    }

    override fun forEachTransform(consumer: (BoneTransformProvider) -> Unit) {
        activeController.forEachTransform(consumer)
    }

    override fun isDeprecatedMode(): Boolean = activeController.isDeprecatedMode()

    override fun reset() {
        if (initialized) {
            animationRuntime.reset()
            controller.reset()
        } else {
            controller.reset()
        }
    }
}