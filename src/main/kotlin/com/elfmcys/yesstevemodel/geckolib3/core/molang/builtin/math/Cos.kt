package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import net.minecraft.util.Mth

open class Cos : Function {
    open fun evaluate(context: ExecutionContext<*>, arguments: ArgumentCollection): Any {
        return Mth.cos(arguments.getAsFloat(context, 0) / 180.0f * 3.1415927f)
    }
    open fun validateArgumentSize(size: Int): Boolean {
        return size == 1
    }
}