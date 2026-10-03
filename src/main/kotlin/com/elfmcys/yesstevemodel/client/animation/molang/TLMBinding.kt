package com.elfmcys.yesstevemodel.client.animation.molang

import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ContextBinding
import com.elfmcys.yesstevemodel.util.data.LazySupplier
import rip.ysm.compat.touhoulittlemaid.TouhouLittleMaidCompat

class TLMBinding private constructor() : ContextBinding() {
    init {
        TouhouLittleMaidCompat.registerMaidAnimStates(this)
    }

    companion object {
        @JvmField
        val INSTANCE: LazySupplier<TLMBinding> = LazySupplier(::TLMBinding)
    }
}