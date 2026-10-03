package com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.variable

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.molang.runtime.AssignableVariable
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.CloseVariable
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap
import org.jetbrains.annotations.NotNull

open class TempVariableRegistry : ObjectBinding, CloseVariable {
    val variableMap: Object2ReferenceMap<String, TempVariable> = Object2ReferenceOpenHashMap()
    var topPointer: Int = 0
    open fun getProperty(str: String): Any {
        return this.variableMap.computeIfAbsent(str, { obj -> this.topPointer = i + 1
return TempVariable(i) })
    }
    open fun dispose() {
        this.variableMap.clear()
        this.topPointer = 0
    }
    open class TempVariable : AssignableVariable {
        var address: Int = 0
        constructor(address: Int) {
            this.address = address
        }
        open fun address(): Int {
            return this.address
        }
        open fun evaluate(context: ExecutionContext<*>): Any {
            return (context.entity() as IContext<Any>).tempStorage().getElement(this.address)
        }
        open fun assign(context: ExecutionContext<*>, value: Any) {
            (context.entity() as IContext<Any>).tempStorage().setElement(this.address, value)
        }
    }
}