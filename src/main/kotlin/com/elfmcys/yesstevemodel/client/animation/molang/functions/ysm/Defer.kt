package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm

import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationControllerContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function.ArgumentCollection

class Defer : ContextFunction<Any>() {
    override fun eval(context: ExecutionContext<IContext<Any>>, arguments: ArgumentCollection): Any? {
        val i: Int = arguments.getStringId(context, 0)
        val animationControllerContext: AnimationControllerContext? = context.entity.animationControllerContext
        if (context.entity.isClientSide && animationControllerContext != null && i != StringPool.EMPTY_ID) {
            animationControllerContext.captureArguments(context, i, arguments, 1)
        }
        return null
    }
}