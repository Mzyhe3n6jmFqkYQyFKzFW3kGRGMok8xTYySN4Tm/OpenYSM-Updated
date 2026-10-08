package com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction
import net.minecraft.client.player.LocalPlayer

abstract class LocalPlayerEntityFunction : ContextFunction<LocalPlayer>() {
    override fun validateContext(context: IContext<*>): Boolean {
        return context.entity is LocalPlayer
    }
}