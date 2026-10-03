package com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction
import net.minecraft.world.entity.TamableAnimal

abstract class TamableEntityFunction : ContextFunction<TamableAnimal>() {
    override fun validateContext(context: IContext<*>): Boolean {
        return context.entity() is TamableAnimal
    }
}