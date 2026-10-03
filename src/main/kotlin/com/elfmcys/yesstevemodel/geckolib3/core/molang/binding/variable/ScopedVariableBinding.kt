package com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.variable

import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ResetVariable
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.molang.runtime.AssignableVariable
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap

class ScopedVariableBinding : ObjectBinding, ResetVariable {
    private val variableMap: Int2ReferenceOpenHashMap<ScopedVariable> = Int2ReferenceOpenHashMap()

    override fun getProperty(name: String): Any? {
        return variableMap.computeIfAbsent(StringPool.computeIfAbsent(name)) { ScopedVariable(it) }
    }

    override fun reset() {
        variableMap.clear()
    }

    private class ScopedVariable(private val name: Int) : AssignableVariable {
        override fun evaluate(context: ExecutionContext<*>): Any? {
            return (context.entity() as? IContext<*>)?.scopedStorage()?.getScoped(name)
        }

        override fun assign(context: ExecutionContext<*>, value: Any?) {
            (context.entity() as? IContext<*>)?.scopedStorage()?.setScoped(name, value)
        }
    }
}