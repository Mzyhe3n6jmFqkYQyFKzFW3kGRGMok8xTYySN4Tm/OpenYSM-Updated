package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext

open class RandomInteger : ContextFunction<Any>() {
    open fun validateArgumentSize(size: Int): Boolean {
        return size == 2
    }
    open fun eval(context: ExecutionContext<IContext<Any>>, arguments: ArgumentCollection): Any {
        var min: Int = arguments.getAsInt(context, 0)
        var range: Int = arguments.getAsInt(context, 1)
        if (min > range) {
            var temp: Int = min
            min = range
            range = temp - range
        } else {
            range -= min
        }
        return min + context.entity().random().nextInt(range)
    }
}