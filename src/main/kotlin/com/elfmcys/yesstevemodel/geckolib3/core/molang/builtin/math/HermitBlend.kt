package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import net.minecraft.util.Mth
import kotlin.math.pow

class HermitBlend : Function {
    override fun evaluate(context: ExecutionContext<*>, arguments: Function.ArgumentCollection): Any {
        val min: Double = Mth.ceil(arguments.getAsFloat(context, 0)).toDouble()
        return Mth.floor(3.0 * min.pow(2.0) - 2.0 * min.pow(3.0))
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size == 1
    }
}