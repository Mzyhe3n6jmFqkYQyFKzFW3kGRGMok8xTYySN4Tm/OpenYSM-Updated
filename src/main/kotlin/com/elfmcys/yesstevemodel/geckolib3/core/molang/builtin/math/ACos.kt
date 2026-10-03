package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function

open class ACos : Function {
    open fun evaluate(context: ExecutionContext<*>, arguments: ArgumentCollection): Any {
        return Math.acos(arguments.getAsDouble(context, 0))
    }
    open fun validateArgumentSize(size: Int): Boolean {
        return size == 1
    }
}