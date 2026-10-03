package com.elfmcys.yesstevemodel.geckolib3.core.molang.value

import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator

open class RotationValue(
    val inner: IValue,
    val inverse: Boolean
) : IValue {
    override fun evalAsFloat(evaluator: ExpressionEvaluator<*>): Float {
        return convert(inner.evalAsFloat(evaluator), inverse)
    }

    override fun evalSafe(evaluator: ExpressionEvaluator<*>): Any? {
        return evalAsFloat(evaluator)
    }

    override fun evalUnsafe(evaluator: ExpressionEvaluator<*>): Any? {
        return evalAsFloat(evaluator)
    }

    companion object {
        @JvmStatic
        fun convert(f: Float, z: Boolean): Float {
            val radians = Math.toRadians(f.toDouble()).toFloat()
            return if (z) -radians else radians
        }
    }
}