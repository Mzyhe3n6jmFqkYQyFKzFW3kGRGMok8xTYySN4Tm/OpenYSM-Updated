package com.elfmcys.yesstevemodel.client.animation.molang.functions.ctrl

import com.elfmcys.yesstevemodel.geckolib3.core.controller.PredicateBasedController
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function.ArgumentCollection

class Reset : ContextFunction<Any>() {
    override fun eval(context: ExecutionContext<IContext<Any>>, arguments: ArgumentCollection): Any? {
        val animationController: PredicateBasedController<*>? = context.entity().animationEvent().controller
        animationController?.clearAnimation()
        return null
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size == 0
    }
}