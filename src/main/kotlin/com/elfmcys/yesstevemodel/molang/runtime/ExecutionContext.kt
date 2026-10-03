package com.elfmcys.yesstevemodel.molang.runtime

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.molang.parser.ast.Expression

interface ExecutionContext<TEntity> {
    fun entity(): TEntity

    fun eval(expression: Expression): Any?

    fun evalAll(iterable: Iterable<Expression>, returnLast: Boolean): Any?

    fun evalSafe(expression: Expression): Any? {
        return runCatching {
            eval(expression)
        }.onFailure { e ->
            Constants.LOGGER.debug("Failed to evaluate molang expression.", e)
        }.getOrNull()
    }

    fun evalAllSafe(iterable: Iterable<Expression>, returnLast: Boolean): Any? {
        return runCatching {
            evalAll(iterable, returnLast)
        }.onFailure { e ->
            Constants.LOGGER.debug("Failed to evaluate molang expression.", e)
        }.getOrNull()
    }
}