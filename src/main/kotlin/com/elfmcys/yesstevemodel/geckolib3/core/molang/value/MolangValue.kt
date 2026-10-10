@file:Suppress("MemberVisibilityCanBePrivate")

package com.elfmcys.yesstevemodel.geckolib3.core.molang.value

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.molang.parser.ast.Expression
import com.elfmcys.yesstevemodel.molang.parser.ast.FloatExpression
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import com.elfmcys.yesstevemodel.molang.runtime.binding.ValueConversions

class MolangValue(
    val expressions: List<Expression>,
    val isScript: Boolean
) : IValue {
    val single: Expression? = if (!isScript && expressions.size == 1) expressions[0] else null
    val constant: Boolean = single is FloatExpression
    val constFloat: Float = if (single is FloatExpression) single.value() else 0.0f
    val constBoxed: Float? = if (single is FloatExpression) single.boxed() else null

    override fun evalAsFloat(evaluator: ExpressionEvaluator<*>): Float {
        if (constant) {
            return constFloat
        }
        val s = single
        if (s != null) {
            return runCatching {
                evaluator.evalAsFloat(s)
            }.getOrElse { th ->
                Constants.LOGGER.debug("Failed to evaluate molang expression.", th)
                0.0f
            }
        }
        return ValueConversions.asFloat(evalSafe(evaluator))
    }

    override fun evalAsBoolean(evaluator: ExpressionEvaluator<*>): Boolean {
        if (constant) {
            return constFloat != 0.0f
        }
        val s = single
        if (s != null) {
            return runCatching {
                evaluator.evalAsBoolean(s)
            }.getOrElse { th ->
                Constants.LOGGER.debug("Failed to evaluate molang expression.", th)
                false
            }
        }
        return ValueConversions.asBoolean(evalSafe(evaluator))
    }

    override fun evalUnsafe(evaluator: ExpressionEvaluator<*>): Any? {
        if (constant) {
            return constBoxed
        }
        return evaluator.evalAll(expressions, isScript)
    }
}