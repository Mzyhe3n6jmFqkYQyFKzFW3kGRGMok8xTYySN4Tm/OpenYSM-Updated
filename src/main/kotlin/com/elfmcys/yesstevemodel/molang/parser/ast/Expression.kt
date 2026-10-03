package com.elfmcys.yesstevemodel.molang.parser.ast

interface Expression {
    fun <R> visit(visitor: ExpressionVisitor<R>): R
}