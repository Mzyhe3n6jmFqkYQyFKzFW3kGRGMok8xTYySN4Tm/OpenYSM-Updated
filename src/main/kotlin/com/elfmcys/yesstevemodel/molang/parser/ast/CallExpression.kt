package com.elfmcys.yesstevemodel.molang.parser.ast

import com.elfmcys.yesstevemodel.molang.runtime.Function
import it.unimi.dsi.fastutil.objects.ObjectLists

class CallExpression(
    val function: Function,
    val arguments: Function.ArgumentCollection = EMPTY
) : Expression {

    fun function(): Function = function
    fun arguments(): Function.ArgumentCollection = arguments

    override fun <R> visit(visitor: ExpressionVisitor<R>): R {
        return visitor.visitCall(this)
    }

    override fun toString(): String {
        return "Call($function, $arguments)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is CallExpression) return false
        return function == other.function && arguments == other.arguments
    }

    override fun hashCode(): Int {
        var result = function.hashCode()
        result = 31 * result + arguments.hashCode()
        return result
    }

    companion object {
        @JvmField
        val EMPTY: Function.ArgumentCollection = Function.ArgumentCollection(ObjectLists.emptyList())
    }
}