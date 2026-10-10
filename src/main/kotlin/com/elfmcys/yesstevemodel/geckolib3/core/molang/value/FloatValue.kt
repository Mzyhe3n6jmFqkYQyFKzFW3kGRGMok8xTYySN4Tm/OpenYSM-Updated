package com.elfmcys.yesstevemodel.geckolib3.core.molang.value

import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator

class FloatValue(val value: Float) : IValue {
    private val boxedValue: Float = if (!value.isNaN()) value else 0.0f

    @Suppress("unused")
    val safeValue: Float
        get() = boxedValue

    override fun evalAsFloat(evaluator: ExpressionEvaluator<*>): Float = boxedValue

    override fun evalAsInt(evaluator: ExpressionEvaluator<*>): Int = boxedValue.toInt()

    override fun evalAsBoolean(evaluator: ExpressionEvaluator<*>): Boolean = boxedValue != 0.0f

    override fun evalSafe(evaluator: ExpressionEvaluator<*>): Any = boxedValue

    override fun evalUnsafe(evaluator: ExpressionEvaluator<*>): Any = boxedValue

    fun value(): Float = boxedValue

    companion object {
        @JvmField
        val ONE: FloatValue = FloatValue(1.0f)

        @JvmField
        val ZERO: FloatValue = FloatValue(0.0f)
    }
}