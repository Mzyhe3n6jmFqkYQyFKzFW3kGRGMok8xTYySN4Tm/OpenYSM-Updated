package com.elfmcys.yesstevemodel.molang.runtime

fun interface Variable {
    fun evaluate(context: ExecutionContext<*>): Any?
}