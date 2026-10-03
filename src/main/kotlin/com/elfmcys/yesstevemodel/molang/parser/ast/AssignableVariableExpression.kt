package com.elfmcys.yesstevemodel.molang.parser.ast

import com.elfmcys.yesstevemodel.molang.runtime.AssignableVariable

open class AssignableVariableExpression(
    val target: AssignableVariable
) : Expression {

    fun target(): AssignableVariable = target

    override fun <R> visit(visitor: ExpressionVisitor<R>): R {
        return visitor.visitAssignableVariable(this)
    }

    override fun toString(): String {
        return target.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is AssignableVariableExpression) return false
        return target == other.target
    }

    override fun hashCode(): Int {
        return target.hashCode()
    }
}