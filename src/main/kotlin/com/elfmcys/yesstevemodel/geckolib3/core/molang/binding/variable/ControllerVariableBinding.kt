package com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.variable

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.storage.IControllerVariableStorage
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ResetVariable
import com.elfmcys.yesstevemodel.molang.runtime.AssignableVariable
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap
import org.jetbrains.annotations.NotNull

open class ControllerVariableBinding : ObjectBinding, ResetVariable {
    val variableMap: Int2ReferenceOpenHashMap<ControllerVariable> = Int2ReferenceOpenHashMap()
    open fun getProperty(name: String): Any {
        return variableMap.computeIfAbsent(StringPool.computeIfAbsent(name), ControllerVariable::new)
    }
    open fun reset() {
        variableMap.clear()
    }
    open class ControllerVariable : AssignableVariable {
        var name: Int = 0
        constructor(name: Int) {
            this.name = name
        }
        open fun name(): Int {
            return this.name
        }
        open fun evaluate(context: ExecutionContext<*>): Any {
            var storage: IControllerVariableStorage = (context.entity() as IContext<Any>).controllerStorage()
            if (storage != null) {
                return storage.getControllerVariable(this.name)
            }
            return null
        }
        open fun assign(context: ExecutionContext<*>, value: Any) {
            var storage: IControllerVariableStorage = (context.entity() as IContext<Any>).controllerStorage()
            if (storage != null) {
                storage.setControllerVariable(this.name, value)
            }
        }
    }
}