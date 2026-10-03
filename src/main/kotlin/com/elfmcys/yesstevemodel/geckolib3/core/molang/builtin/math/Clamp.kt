package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import net.minecraft.util.Mth

class Clamp : Function {
    override fun evaluate(context: ExecutionContext<*>, arguments: Function.ArgumentCollection): Any {
        return Mth.clamp(arguments.getAsFloat(context, 0), arguments.getAsFloat(context, 1), arguments.getAsFloat(context, 2))
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size == 3
    }
}