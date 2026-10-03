package com.elfmcys.yesstevemodel.client.animation.molang

import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ContextBinding
import rip.ysm.compat.touhoulittlemaid.TouhouLittleMaidCompat

object TLMBinding : ContextBinding() {
    init {
        TouhouLittleMaidCompat.registerMaidAnimStates(this)
    }
}