package com.elfmcys.yesstevemodel.geckolib3.core.molang.binding

import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.variable.ControllerVariableBinding
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.variable.ScopedVariableBinding
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.variable.TempVariableRegistry
import com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.MathBinding
import com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.QueryBinding
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding
import com.elfmcys.yesstevemodel.molang.runtime.binding.StandardBindings
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap

open class PrimaryBinding(map: MutableMap<String, Any>?) : ObjectBinding {
    @JvmField val bindings: Object2ReferenceOpenHashMap<String, Any> = Object2ReferenceOpenHashMap()
    @JvmField val scopedBinding: ScopedVariableBinding = ScopedVariableBinding()
    @JvmField val foreignBinding: ControllerVariableBinding = ControllerVariableBinding()
    @JvmField val tempBinding: TempVariableRegistry = TempVariableRegistry()
    val closeables: List<CloseVariable>
    val resettables: List<ResetVariable>

    init {
        if (map != null) {
            bindings.putAll(map)
        }
        bindings.put("math", MathBinding)
        bindings.put("query", QueryBinding)
        bindings.put("q", QueryBinding)
        bindings.put("loop", StandardBindings.LOOP_FUNC)
        bindings.put("for_each", StandardBindings.FOR_EACH_FUNC)
        bindings.put("variable", scopedBinding)
        bindings.put("v", scopedBinding)
        bindings.put("context", foreignBinding)
        bindings.put("c", foreignBinding)
        bindings.put("temp", tempBinding)
        bindings.put("t", tempBinding)
        closeables = bindings.values.filterIsInstance<CloseVariable>()
        resettables = bindings.values.filterIsInstance<ResetVariable>()
    }

    override fun getProperty(name: String): Any? = bindings.get(name)

    open fun reset() {
        for (resetVariable in resettables) {
            resetVariable.reset()
        }
    }

    open fun dispose() {
        for (closeVariable in closeables) {
            closeVariable.dispose()
        }
    }
}