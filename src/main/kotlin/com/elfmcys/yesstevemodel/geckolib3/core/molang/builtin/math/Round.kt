package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import kotlin.math.roundToInt

class Round : Function {
    override fun evaluate(context: ExecutionContext<*>, arguments: Function.ArgumentCollection): Any {
        return arguments.getAsFloat(context, 0).roundToInt()
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size == 1
    }
}