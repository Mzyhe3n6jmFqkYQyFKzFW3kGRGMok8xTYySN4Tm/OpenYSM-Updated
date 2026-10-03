package com.elfmcys.yesstevemodel.geckolib3.core.molang.value

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import com.elfmcys.yesstevemodel.molang.runtime.binding.ValueConversions

fun interface IValue {
    fun evalUnsafe(evaluator: ExpressionEvaluator<*>): Any?

    fun evalAsFloat(evaluator: ExpressionEvaluator<*>): Float {
        return ValueConversions.asFloat(evalSafe(evaluator))
    }

    fun evalAsInt(evaluator: ExpressionEvaluator<*>): Int {
        return ValueConversions.asInt(evalSafe(evaluator))
    }

    fun evalAsBoolean(evaluator: ExpressionEvaluator<*>): Boolean {
        return ValueConversions.asBoolean(evalSafe(evaluator))
    }

    fun evalSafe(evaluator: ExpressionEvaluator<*>): Any? {
        return runCatching {
            evalUnsafe(evaluator)
        }.onFailure { th ->
            Constants.LOGGER.debug("Failed to evaluate molang expression.", th)
        }.getOrNull()
    }
}