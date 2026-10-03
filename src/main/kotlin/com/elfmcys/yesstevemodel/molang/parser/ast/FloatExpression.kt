package com.elfmcys.yesstevemodel.molang.parser.ast

class FloatExpression(val value: Float) : Expression {
    val boxed: Float = value

    fun value(): Float = value
    fun boxed(): Float = boxed

    override fun <R> visit(visitor: ExpressionVisitor<R>): R {
        return visitor.visitFloat(this)
    }

    override fun toString(): String {
        return value.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is FloatExpression) return false
        return value.compareTo(other.value) == 0
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    companion object {
        @JvmField
        val ZERO: FloatExpression = FloatExpression(0.0f)

        @JvmField
        val ONE: FloatExpression = FloatExpression(1.0f)
    }
}