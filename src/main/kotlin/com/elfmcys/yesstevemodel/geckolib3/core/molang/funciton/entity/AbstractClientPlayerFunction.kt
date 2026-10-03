package com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction
import net.minecraft.client.player.AbstractClientPlayer

abstract class AbstractClientPlayerFunction : ContextFunction<AbstractClientPlayer>() {
    override fun validateContext(context: IContext<*>): Boolean {
        return context.entity() is AbstractClientPlayer
    }
}