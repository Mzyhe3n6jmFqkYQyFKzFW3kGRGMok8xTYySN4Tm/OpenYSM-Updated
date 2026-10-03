package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import net.minecraft.util.Mth

open class Trunc : Function {
    open fun evaluate(context: ExecutionContext<*>, arguments: ArgumentCollection): Any {
        var value: Float = arguments.getAsFloat(context, 0)
        return if (value < 0) Mth.ceil(value) else Mth.floor(value)
    }
    open fun validateArgumentSize(size: Int): Boolean {
        return size == 1
    }
}