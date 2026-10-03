package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import kotlin.math.acos

class ACos : Function {
    override fun evaluate(context: ExecutionContext<*>, arguments: Function.ArgumentCollection): Any {
        return acos(arguments.getAsDouble(context, 0))
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size == 1
    }
}