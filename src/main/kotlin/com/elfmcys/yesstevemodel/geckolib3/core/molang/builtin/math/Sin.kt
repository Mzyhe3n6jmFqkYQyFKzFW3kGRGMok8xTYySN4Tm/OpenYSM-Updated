package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import net.minecraft.util.Mth

class Sin : Function {
    override fun evaluate(context: ExecutionContext<*>, arguments: Function.ArgumentCollection): Any {
        return Mth.sin((arguments.getAsFloat(context, 0) / 180.0f * 3.1415927f).toDouble())
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size == 1
    }
}