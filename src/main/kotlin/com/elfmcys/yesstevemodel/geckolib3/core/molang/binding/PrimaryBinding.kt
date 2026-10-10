package com.elfmcys.yesstevemodel.geckolib3.core.molang.binding

import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.variable.ControllerVariableBinding
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.variable.ScopedVariableBinding
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.variable.TempVariableRegistry
import com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.MathBinding
import com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.QueryBinding
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding
import com.elfmcys.yesstevemodel.molang.runtime.binding.StandardBindings
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap

class PrimaryBinding(map: MutableMap<String, Any>?) : ObjectBinding {
    val bindings: Object2ReferenceOpenHashMap<String, Any> = Object2ReferenceOpenHashMap()

    val scopedBinding: ScopedVariableBinding = ScopedVariableBinding()

    val foreignBinding: ControllerVariableBinding = ControllerVariableBinding()

    val tempBinding: TempVariableRegistry = TempVariableRegistry()
    val closeables: List<CloseVariable>
    val resettables: List<ResetVariable>

    init {
        if (map != null) {
            bindings.putAll(map)
        }
        bindings["math"] = MathBinding
        bindings["query"] = QueryBinding
        bindings["q"] = QueryBinding
        bindings["loop"] = StandardBindings.LOOP_FUNC
        bindings["for_each"] = StandardBindings.FOR_EACH_FUNC
        bindings["variable"] = scopedBinding
        bindings["v"] = scopedBinding
        bindings["context"] = foreignBinding
        bindings["c"] = foreignBinding
        bindings["temp"] = tempBinding
        bindings["t"] = tempBinding
        closeables = bindings.values.filterIsInstance<CloseVariable>()
        resettables = bindings.values.filterIsInstance<ResetVariable>()
    }

    override fun getProperty(name: String): Any? = bindings[name]

    fun reset() {
        for (resetVariable in resettables) {
            resetVariable.reset()
        }
    }

    fun dispose() {
        for (closeVariable in closeables) {
            closeVariable.dispose()
        }
    }
}