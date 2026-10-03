package com.elfmcys.yesstevemodel.molang.runtime

interface AssignableVariable : Variable {
    fun assign(context: ExecutionContext<*>, value: Any?)
}