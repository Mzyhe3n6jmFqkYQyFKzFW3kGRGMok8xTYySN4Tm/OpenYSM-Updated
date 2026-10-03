package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import kotlin.math.pow

class Pow : Function {
    override fun evaluate(context: ExecutionContext<*>, arguments: Function.ArgumentCollection): Any {
        return arguments.getAsDouble(context, 0).pow(arguments.getAsDouble(context, 1))
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size == 2
    }
}