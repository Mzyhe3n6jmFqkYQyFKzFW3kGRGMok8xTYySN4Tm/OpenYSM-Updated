package com.elfmcys.yesstevemodel.client.animation.molang.functions.ctrl

import com.elfmcys.yesstevemodel.geckolib3.core.controller.PredicateBasedController
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function.ArgumentCollection

class SetTransitionSpeed : ContextFunction<Any>() {
    override fun eval(context: ExecutionContext<IContext<Any>>, arguments: ArgumentCollection): Any? {
        val animationController: PredicateBasedController<*>? = context.entity().animationEvent().controller
        if (animationController == null) {
            return null
        }
        val second: Float = arguments.getAsFloat(context, 0)
        if (second < 0.0f) {
            return null
        }
        animationController.setTransitionLengthTicks(second)
        return null
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size == 1
    }
}