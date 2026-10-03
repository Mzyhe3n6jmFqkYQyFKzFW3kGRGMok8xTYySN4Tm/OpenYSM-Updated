package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import net.minecraft.util.Mth

class Trunc : Function {
    override fun evaluate(context: ExecutionContext<*>, arguments: Function.ArgumentCollection): Any {
        val value: Float = arguments.getAsFloat(context, 0)
        return if (value < 0) Mth.ceil(value) else Mth.floor(value)
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size == 1
    }
}