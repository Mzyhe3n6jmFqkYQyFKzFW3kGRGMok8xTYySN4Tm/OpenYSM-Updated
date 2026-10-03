package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import kotlin.math.exp

class Exp : Function {
    override fun evaluate(context: ExecutionContext<*>, arguments: Function.ArgumentCollection): Any {
        return exp(arguments.getAsDouble(context, 0))
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size == 1
    }
}