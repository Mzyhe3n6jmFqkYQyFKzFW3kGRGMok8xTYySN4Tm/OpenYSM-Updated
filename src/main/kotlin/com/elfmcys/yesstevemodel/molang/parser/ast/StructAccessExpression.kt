package com.elfmcys.yesstevemodel.molang.parser.ast

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool

open class StructAccessExpression(
    val left: Expression,
    val path: Int
) : Expression {

    constructor(expression: Expression, path: String) : this(expression, StringPool.computeIfAbsent(path))

    override fun <R> visit(visitor: ExpressionVisitor<R>): R {
        return visitor.visitStruct(this)
    }

    fun left(): Expression = left
    fun path(): Int = path
}