package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math

import com.elfmcys.yesstevemodel.geckolib3.util.Interpolations
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function

class Lerp : Function {
    override fun evaluate(context: ExecutionContext<*>, arguments: Function.ArgumentCollection): Any {
        return Interpolations.lerp(
            arguments.getAsFloat(context, 0),
            arguments.getAsFloat(context, 1),
            arguments.getAsFloat(context, 2)
        )
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size == 3
    }
}