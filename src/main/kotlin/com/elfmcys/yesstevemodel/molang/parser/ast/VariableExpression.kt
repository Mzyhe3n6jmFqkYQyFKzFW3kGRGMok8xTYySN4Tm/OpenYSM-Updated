package com.elfmcys.yesstevemodel.molang.parser.ast

import com.elfmcys.yesstevemodel.molang.runtime.Variable

open class VariableExpression(
    val target: Variable
) : Expression {

    fun target(): Variable = target

    override fun <R> visit(visitor: ExpressionVisitor<R>): R {
        return visitor.visitVariable(this)
    }

    override fun toString(): String {
        return target.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is VariableExpression) return false
        return target == other.target
    }

    override fun hashCode(): Int {
        return target.hashCode()
    }
}