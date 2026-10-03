package com.elfmcys.yesstevemodel.molang.parser.ast

import com.elfmcys.yesstevemodel.molang.runtime.AssignableVariable
import com.elfmcys.yesstevemodel.molang.runtime.Function
import com.elfmcys.yesstevemodel.molang.runtime.Variable

class IdentifierExpression(
    name: String,
    val target: Any?
) : Expression {
    val name: String = name.lowercase()

    fun name(): String = name
    fun target(): Any? = target

    override fun <R> visit(visitor: ExpressionVisitor<R>): R {
        return visitor.visitIdentifier(this)
    }

    override fun toString(): String {
        return "Identifier($name)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is IdentifierExpression) return false
        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    companion object {
        @JvmStatic
        fun get(name: String, target: Any): Expression {
            return when (target) {
                is Number -> FloatExpression(target.toFloat())
                is String -> StringExpression(target)
                is Function -> CallExpression(target)
                is AssignableVariable -> AssignableVariableExpression(target)
                is Variable -> VariableExpression(target)
                else -> IdentifierExpression(name, target)
            }
        }
    }
}