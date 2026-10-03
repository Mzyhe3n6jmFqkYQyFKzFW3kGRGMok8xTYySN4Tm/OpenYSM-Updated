package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import kotlin.math.abs

class Abs : Function {
    override fun evaluate(context: ExecutionContext<*>, arguments: Function.ArgumentCollection): Any {
        return abs(arguments.getAsFloat(context, 0))
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size == 1
    }
}