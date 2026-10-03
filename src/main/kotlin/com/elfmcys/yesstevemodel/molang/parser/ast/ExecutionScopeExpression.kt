package com.elfmcys.yesstevemodel.molang.parser.ast

class ExecutionScopeExpression(
    val expressions: List<Expression>
) : Expression {

    fun expressions(): List<Expression> = expressions

    override fun <R> visit(visitor: ExpressionVisitor<R>): R {
        return visitor.visitExecutionScope(this)
    }

    override fun toString(): String {
        return "ExecutionScope($expressions)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is ExecutionScopeExpression) return false
        return expressions == other.expressions
    }

    override fun hashCode(): Int {
        return expressions.hashCode()
    }
}