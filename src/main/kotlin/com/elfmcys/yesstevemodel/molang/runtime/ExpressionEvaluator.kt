package com.elfmcys.yesstevemodel.molang.runtime

import com.elfmcys.yesstevemodel.molang.parser.ast.Expression
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding
import com.elfmcys.yesstevemodel.molang.runtime.binding.ValueConversions

interface ExpressionEvaluator<TEntity> : ExecutionContext<TEntity> {
    fun evalAsFloat(expression: Expression): Float {
        return ValueConversions.asFloat(eval(expression))
    }

    fun evalAsBoolean(expression: Expression): Boolean {
        return ValueConversions.asBoolean(eval(expression))
    }

    companion object {
        fun <TEntity> evaluator(entity: TEntity): ExpressionEvaluator<TEntity> {
            return ExpressionEvaluatorImpl(entity)
        }

        fun evaluator(): ExpressionEvaluator<ObjectBinding> {
            return evaluator(ObjectBinding.EMPTY)
        }
    }
}