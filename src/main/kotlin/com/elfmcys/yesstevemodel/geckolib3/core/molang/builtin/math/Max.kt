package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import kotlin.math.max

class Max : Function {
    override fun evaluate(context: ExecutionContext<*>, arguments: Function.ArgumentCollection): Any {
        return max(arguments.getAsFloat(context, 0), arguments.getAsFloat(context, 1))
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size == 2
    }
}