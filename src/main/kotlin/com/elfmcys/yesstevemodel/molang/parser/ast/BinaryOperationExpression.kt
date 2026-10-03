package com.elfmcys.yesstevemodel.molang.parser.ast

open class BinaryOperationExpression(
    @JvmField val left: Expression,
    @JvmField val right: Expression
) : Expression {

    override fun <R> visit(visitor: ExpressionVisitor<R>): R {
        return visitor.visitBinaryOperation(this)
    }

    fun getLeft(): Expression = left
    fun getRight(): Expression = right
}