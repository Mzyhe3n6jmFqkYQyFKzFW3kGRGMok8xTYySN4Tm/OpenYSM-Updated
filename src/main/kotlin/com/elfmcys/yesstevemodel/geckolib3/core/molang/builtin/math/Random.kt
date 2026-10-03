package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext

open class Random : ContextFunction<Any>() {
    open fun validateArgumentSize(size: Int): Boolean {
        return size == 2 || size == 3
    }
    open fun eval(context: ExecutionContext<IContext<Any>>, arguments: ArgumentCollection): Any {
        var min: Float = arguments.getAsFloat(context, 0)
        var range: Float = arguments.getAsFloat(context, 1)
        if (min > range) {
            var temp: Float = min
            min = range
            range = temp - range
        } else {
            range -= min
        }
        return min + context.entity().random().nextFloat() * range
    }
}