package com.elfmcys.yesstevemodel.molang.parser.ast

import java.util.*

class TernaryConditionalExpression(
    val conditional: Expression,
    val trueExpression: Expression,
    val falseExpression: Expression
) : Expression {

    fun condition(): Expression = conditional
    fun trueExpression(): Expression = trueExpression
    fun falseExpression(): Expression = falseExpression

    override fun <R> visit(visitor: ExpressionVisitor<R>): R {
        return visitor.visitTernaryConditional(this)
    }

    override fun toString(): String {
        return "TernaryCondition($conditional, $trueExpression, $falseExpression)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is TernaryConditionalExpression) return false
        return conditional == other.conditional && trueExpression == other.trueExpression && falseExpression == other.falseExpression
    }

    override fun hashCode(): Int {
        return Objects.hash(conditional, trueExpression, falseExpression)
    }
}