package com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.variable

import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ResetVariable
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Variable
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap

class ForeignVariableBinding : ObjectBinding, ResetVariable {
    private val variableMap: Int2ReferenceOpenHashMap<ForeignVariable> = Int2ReferenceOpenHashMap()

    override fun getProperty(name: String): Any? {
        return variableMap.computeIfAbsent(StringPool.computeIfAbsent(name)) { ForeignVariable(it) }
    }

    override fun reset() {
        variableMap.clear()
    }

    private class ForeignVariable(private val name: Int) : Variable {
        override fun evaluate(context: ExecutionContext<*>): Any? {
            val storage = (context.entity as? IContext<*>)?.foreignStorage()
            return storage?.getPublic(name)
        }
    }
}