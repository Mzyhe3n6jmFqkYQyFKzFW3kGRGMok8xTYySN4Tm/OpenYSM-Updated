package com.elfmcys.yesstevemodel.molang.parser.ast

class BinaryExpression(
    val op: Op,
    val left: Expression,
    val right: Expression
) : Expression {

    fun op(): Op = op
    fun left(): Expression = left
    fun right(): Expression = right

    override fun <R> visit(visitor: ExpressionVisitor<R>): R {
        return visitor.visitBinary(this)
    }

    override fun toString(): String {
        return "${op.name}($left, $right)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is BinaryExpression) return false
        return op == other.op && left == other.left && right == other.right
    }

    override fun hashCode(): Int {
        var result = op.hashCode()
        result = 31 * result + left.hashCode()
        result = 31 * result + right.hashCode()
        return result
    }

    enum class Op(val precedence: Int, val index: Int) {
        AND(1800, 0),
        OR(1600, 1),
        LT(2200, 2),
        LTE(2200, 3),
        GT(2200, 4),
        GTE(2200, 5),
        ADD(2400, 6),
        SUB(2400, 7),
        MUL(2600, 8),
        DIV(2600, 9),
        ARROW(3000, 10),
        NULL_COALESCE(1200, 11),
        ASSIGN(1, 12),
        CONDITIONAL(1400, 13),
        EQ(2000, 14),
        NEQ(2000, 15);

        fun precedence(): Int = precedence
        fun index(): Int = index
    }
}