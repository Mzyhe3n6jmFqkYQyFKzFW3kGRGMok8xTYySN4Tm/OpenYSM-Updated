package com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.variable

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.storage.IForeignVariableStorage
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ResetVariable
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Variable
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap
import org.jetbrains.annotations.NotNull

open class ForeignVariableBinding : ObjectBinding, ResetVariable {
    val variableMap: Int2ReferenceOpenHashMap<ForeignVariable> = Int2ReferenceOpenHashMap()
    open fun getProperty(name: String): Any {
        return this.variableMap.computeIfAbsent(StringPool.computeIfAbsent(name), ForeignVariable::new)
    }
    open fun reset() {
        this.variableMap.clear()
    }
    open class ForeignVariable : Variable {
        var name: Int = 0
        constructor(name: Int) {
            this.name = name
        }
        open fun name(): Int {
            return this.name
        }
        open fun evaluate(context: ExecutionContext<*>): Any {
            var storage: IForeignVariableStorage = (context.entity() as IContext<Any>).foreignStorage()
            if (storage != null) {
                return storage.getPublic(this.name)
            }
            return null
        }
    }
}