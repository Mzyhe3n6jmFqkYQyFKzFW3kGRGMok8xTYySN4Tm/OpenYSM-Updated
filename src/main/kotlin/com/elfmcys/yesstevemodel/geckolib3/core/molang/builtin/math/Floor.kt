package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import net.minecraft.util.Mth

open class Floor : Function {
    open fun evaluate(context: ExecutionContext<*>, arguments: ArgumentCollection): Any {
        return Mth.floor(arguments.getAsFloat(context, 0))
    }
    open fun validateArgumentSize(size: Int): Boolean {
        return size == 1
    }
}