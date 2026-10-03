package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function

open class Max : Function {
    open fun evaluate(context: ExecutionContext<*>, arguments: ArgumentCollection): Any {
        return Math.max(arguments.getAsFloat(context, 0), arguments.getAsFloat(context, 1))
    }
    open fun validateArgumentSize(size: Int): Boolean {
        return size == 2
    }
}