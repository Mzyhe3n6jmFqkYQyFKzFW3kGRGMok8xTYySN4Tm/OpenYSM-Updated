package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import net.minecraft.util.RandomSource

open class DieRoll : ContextFunction<Any>() {
    open fun validateArgumentSize(size: Int): Boolean {
        return size == 3
    }
    open fun eval(context: ExecutionContext<IContext<Any>>, arguments: ArgumentCollection): Any {
        var i: Int = arguments.getAsInt(context, 0)
        var min: Float = arguments.getAsFloat(context, 1)
        var range: Float = arguments.getAsFloat(context, 2)
        if (min > range) {
            var temp: Float = min
            min = range
            range = temp - range
        } else {
            range -= min
        }
        var total: Float = 0
        var rnd: RandomSource = context.entity().random()
        while (i-- > 0) {
            total += min + rnd.nextFloat() * range
        }
        return total
    }
}