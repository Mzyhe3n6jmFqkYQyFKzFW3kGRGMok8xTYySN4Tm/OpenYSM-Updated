package com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.variable

import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ResetVariable
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.molang.runtime.AssignableVariable
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap

class ControllerVariableBinding : ObjectBinding, ResetVariable {
    private val variableMap: Int2ReferenceOpenHashMap<ControllerVariable> = Int2ReferenceOpenHashMap()

    override fun getProperty(name: String): Any? {
        return variableMap.computeIfAbsent(StringPool.computeIfAbsent(name)) { ControllerVariable(it) }
    }

    override fun reset() {
        variableMap.clear()
    }

    private class ControllerVariable(private val name: Int) : AssignableVariable {
        override fun evaluate(context: ExecutionContext<*>): Any? {
            val storage = (context.entity as? IContext<*>)?.controllerStorage
            return storage?.getControllerVariable(name)
        }

        override fun assign(context: ExecutionContext<*>, value: Any?) {
            val storage = (context.entity as? IContext<*>)?.controllerStorage
            storage?.setControllerVariable(name, value)
        }
    }
}