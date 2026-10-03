package com.elfmcys.yesstevemodel.molang.runtime.binding

import com.elfmcys.yesstevemodel.molang.parser.ast.AssignableVariableExpression
import com.elfmcys.yesstevemodel.molang.parser.ast.ExecutionScopeExpression
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluatorImpl
import com.elfmcys.yesstevemodel.molang.runtime.Function
import kotlin.math.roundToLong

object StandardBindings {
    private const val MAX_LOOP_ROUND: Int = 1024

    @JvmField
    val LOOP_FUNC: Function = Function { ctx, args ->
        if (args.size() < 2) {
            return@Function null
        }

        val n = args.getAsDouble(ctx, 0).roundToLong().toInt().coerceAtMost(MAX_LOOP_ROUND)
        val expr = args.getExpression(1)

        if (expr is ExecutionScopeExpression && ctx is ExpressionEvaluatorImpl<*>) {
            ctx.loopFunciton(expr, n)
        }
        null
    }

    @JvmField
    val FOR_EACH_FUNC: Function = Function { ctx, args ->
        if (args.size() != 3) {
            return@Function null
        }
        val variableExpr = args.getExpression(0)
        if (variableExpr !is AssignableVariableExpression) {
            return@Function null
        }
        val variableAccess = variableExpr.target()

        val expr = args.getExpression(2)
        if (expr is ExecutionScopeExpression && ctx is ExpressionEvaluatorImpl<*>) {
            val obj = args.getValue(ctx, 1)
            if (obj is Iterable<*>) {
                ctx.forEachFunction(expr, variableAccess, obj)
            }
        }
        null
    }
}