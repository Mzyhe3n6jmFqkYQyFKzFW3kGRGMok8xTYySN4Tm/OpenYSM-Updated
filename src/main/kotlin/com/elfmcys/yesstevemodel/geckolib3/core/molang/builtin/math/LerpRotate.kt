package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math

import com.elfmcys.yesstevemodel.geckolib3.util.Interpolations
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function

open class LerpRotate : Function {
    open fun evaluate(context: ExecutionContext<*>, arguments: ArgumentCollection): Any {
        return Interpolations.lerpYaw(arguments.getAsFloat(context, 0), arguments.getAsFloat(context, 1), arguments.getAsFloat(context, 2))
    }
    open fun validateArgumentSize(size: Int): Boolean {
        return size == 3
    }
}