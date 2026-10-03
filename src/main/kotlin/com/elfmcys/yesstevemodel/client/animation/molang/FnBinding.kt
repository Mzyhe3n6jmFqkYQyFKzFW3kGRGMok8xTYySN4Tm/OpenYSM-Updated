package com.elfmcys.yesstevemodel.client.animation.molang

import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ResetVariable
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap

class FnBinding : ObjectBinding, ResetVariable {
    private val functions: Object2ReferenceOpenHashMap<String, Fn> = Object2ReferenceOpenHashMap()

    override fun getProperty(name: String): Function {
        return functions.computeIfAbsent(name) { key: String -> Fn(key) }
    }

    override fun reset() {
        functions.clear()
    }

    private class Fn(private var functionName: String?) : Function {
        private var cachedIValue: IValue? = null

        override fun evaluate(context: ExecutionContext<*>, arguments: Function.ArgumentCollection): Any? {
            val entity: Any? = context.entity()
            if (entity is IContext<*>) {
                var value = cachedIValue
                if (value == null) {
                    val fnName = functionName ?: return null
                    val resolved = entity.resolveExpression(fnName)
                    if (resolved == null) {
                        entity.logWarning("User function not found: %s", fnName)
                        functionName = null
                        return null
                    }
                    cachedIValue = resolved
                    value = resolved
                }
                return entity.callFunctionWithArgs(context, value, arguments)
            }
            return null
        }
    }
}