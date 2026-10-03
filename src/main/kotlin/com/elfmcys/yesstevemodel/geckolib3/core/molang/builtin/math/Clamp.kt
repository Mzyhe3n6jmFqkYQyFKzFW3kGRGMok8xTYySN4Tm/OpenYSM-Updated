package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import net.minecraft.util.Mth

open class Clamp : Function {
    open fun evaluate(context: ExecutionContext<*>, arguments: ArgumentCollection): Any {
        return Mth.clamp(arguments.getAsFloat(context, 0), arguments.getAsFloat(context, 1), arguments.getAsFloat(context, 2))
    }
    open fun validateArgumentSize(size: Int): Boolean {
        return size == 3
    }
}