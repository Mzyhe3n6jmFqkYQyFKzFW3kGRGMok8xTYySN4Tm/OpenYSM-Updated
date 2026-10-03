package com.elfmcys.yesstevemodel.molang.parser.ast

class StatementExpression(val op: Op) : Expression {
    override fun <R> visit(visitor: ExpressionVisitor<R>): R = visitor.visitStatement(this)
    override fun toString(): String = op.name
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is StatementExpression) return false
        return op == other.op
    }

    override fun hashCode(): Int = op.hashCode()

    enum class Op {
        BREAK,
        CONTINUE
    }
}