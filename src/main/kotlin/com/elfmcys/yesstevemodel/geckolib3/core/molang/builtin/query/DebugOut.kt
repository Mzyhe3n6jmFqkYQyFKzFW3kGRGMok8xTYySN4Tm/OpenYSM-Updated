package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function

class DebugOut : ContextFunction<Any>() {
    override fun eval(context: ExecutionContext<IContext<Any>>, arguments: Function.ArgumentCollection): Any? {
        if (!context.entity.isDebugMode) {
            return null
        }
        val sb = StringBuilder()
        for (i in 0 until arguments.size()) {
            val value: Any? = arguments.getValue(context, i)
            sb.append(value ?: "null")
        }
        context.entity.logWarning(sb.toString())
        return null
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size > 0
    }
}