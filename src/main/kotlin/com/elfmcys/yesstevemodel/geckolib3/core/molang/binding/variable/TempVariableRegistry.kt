package com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.variable

import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.CloseVariable
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.molang.runtime.AssignableVariable
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap

class TempVariableRegistry : ObjectBinding, CloseVariable {
    private val variableMap: Object2ReferenceMap<String, TempVariable> = Object2ReferenceOpenHashMap()
    private var topPointer: Int = 0

    override fun getProperty(name: String): Any? {
        return variableMap.computeIfAbsent(name) {
            TempVariable(topPointer++)
        }
    }

    override fun dispose() {
        variableMap.clear()
        topPointer = 0
    }

    private class TempVariable(private val address: Int) : AssignableVariable {
        override fun evaluate(context: ExecutionContext<*>): Any? {
            return (context.entity as? IContext<*>)?.tempStorage()?.getElement(address)
        }

        override fun assign(context: ExecutionContext<*>, value: Any?) {
            (context.entity as? IContext<*>)?.tempStorage()?.setElement(address, value)
        }
    }
}