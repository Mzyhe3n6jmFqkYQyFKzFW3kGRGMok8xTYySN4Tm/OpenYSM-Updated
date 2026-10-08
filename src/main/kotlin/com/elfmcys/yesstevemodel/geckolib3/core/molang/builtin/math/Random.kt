package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import net.minecraft.util.RandomSource

class Random : ContextFunction<Any>() {
    override fun validateArgumentSize(size: Int): Boolean {
        return size == 2 || size == 3
    }

    override fun eval(context: ExecutionContext<IContext<Any>>, arguments: Function.ArgumentCollection): Any {
        var min: Float = arguments.getAsFloat(context, 0)
        var range: Float = arguments.getAsFloat(context, 1)
        if (min > range) {
            val temp: Float = min
            min = range
            range = temp - range
        } else {
            range -= min
        }
        val rnd: RandomSource = context.entity.random() ?: RandomSource.create()
        return min + rnd.nextFloat() * range
    }
}