package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import net.minecraft.util.Mth

open class HermitBlend : Function {
    open fun evaluate(context: ExecutionContext<*>, arguments: ArgumentCollection): Any {
        var min: Double = Mth.ceil(arguments.getAsFloat(context, 0))
        return Mth.floor(3.0 * Math.pow(min, 2.0) - 2.0 * Math.pow(min, 3.0))
    }
    open fun validateArgumentSize(size: Int): Boolean {
        return size == 1
    }
}