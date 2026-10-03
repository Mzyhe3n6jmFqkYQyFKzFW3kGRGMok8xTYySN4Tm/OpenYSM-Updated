package com.elfmcys.yesstevemodel.molang.parser.ast

class UnaryExpression(
    val op: Op,
    val expression: Expression
) : Expression {

    fun op(): Op = op
    fun expression(): Expression = expression

    override fun <R> visit(visitor: ExpressionVisitor<R>): R {
        return visitor.visitUnary(this)
    }

    override fun toString(): String {
        return "Unary($op)($expression)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is UnaryExpression) return false
        return op == other.op && expression == other.expression
    }

    override fun hashCode(): Int {
        var result = op.hashCode()
        result = 31 * result + expression.hashCode()
        return result
    }

    enum class Op(val precedence: Int) {
        LOGICAL_NEGATION(2800),
        ARITHMETICAL_NEGATION(2800),
        PLUS(2800),
        RETURN(-1);

        fun precedence(): Int = precedence
    }
}