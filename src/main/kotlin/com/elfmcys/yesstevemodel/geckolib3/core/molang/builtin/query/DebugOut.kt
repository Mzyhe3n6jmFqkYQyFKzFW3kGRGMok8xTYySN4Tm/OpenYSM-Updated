package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext

open class DebugOut : ContextFunction<Any>() {
    open fun eval(context: ExecutionContext<IContext<Any>>, arguments: ArgumentCollection): Any {
        if (!context.entity().isDebugMode()) {
            return null
        }
        var sb: StringBuilder = StringBuilder()
        var i = 0
        while (i < arguments.size()) {
            var value: Any = arguments.getValue(context, i)
            sb.append(if (value == null) "null" else value)
            i++
        }
        context.entity().logWarning(sb.toString())
        return null
    }
    open fun validateArgumentSize(size: Int): Boolean {
        return size > 0
    }
}