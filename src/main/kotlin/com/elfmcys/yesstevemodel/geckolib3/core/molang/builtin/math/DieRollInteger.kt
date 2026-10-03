package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import net.minecraft.util.RandomSource

open class DieRollInteger : ContextFunction<Any>() {
    open fun validateArgumentSize(size: Int): Boolean {
        return size == 3
    }
    open fun eval(context: ExecutionContext<IContext<Any>>, arguments: ArgumentCollection): Any {
        var i: Int = Math.round(arguments.getAsFloat(context, 0))
        var min: Int = arguments.getAsInt(context, 1)
        var range: Int = arguments.getAsInt(context, 2)
        if (min > range) {
            var temp: Int = min
            min = range
            range = temp - range
        } else {
            range -= min
        }
        var total: Int = 0
        var rnd: RandomSource = context.entity().random()
        while (i-- > 0) {
            total += min + rnd.nextInt(range)
        }
        return total
    }
}