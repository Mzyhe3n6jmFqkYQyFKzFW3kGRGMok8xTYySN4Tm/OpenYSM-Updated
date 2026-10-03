package com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction
import net.minecraft.world.entity.LivingEntity

abstract class LivingEntityFunction : ContextFunction<LivingEntity>() {
    override fun validateContext(context: IContext<*>): Boolean {
        return context.entity() is LivingEntity
    }
}