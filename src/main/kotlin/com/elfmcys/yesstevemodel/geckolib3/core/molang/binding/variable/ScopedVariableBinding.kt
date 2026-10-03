package com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.variable

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ResetVariable
import com.elfmcys.yesstevemodel.molang.runtime.AssignableVariable
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap
import org.jetbrains.annotations.NotNull

open class ScopedVariableBinding : ObjectBinding, ResetVariable {
    val variableMap: Int2ReferenceOpenHashMap<ScopedVariable> = Int2ReferenceOpenHashMap()
    open fun getProperty(name: String): Any {
        return variableMap.computeIfAbsent(StringPool.computeIfAbsent(name), ScopedVariable::new)
    }
    open fun reset() {
        this.variableMap.clear()
    }
    open class ScopedVariable : AssignableVariable {
        var name: Int = 0
        constructor(name: Int) {
            this.name = name
        }
        open fun name(): Int {
            return this.name
        }
        open fun evaluate(context: ExecutionContext<*>): Any {
            return (context.entity() as IContext<Any>).scopedStorage().getScoped(name)
        }
        open fun assign(context: ExecutionContext<*>, value: Any) {
            (context.entity() as IContext<Any>).scopedStorage().setScoped(name, value)
        }
    }
}