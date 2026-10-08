package com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction
import net.minecraft.world.entity.Entity

abstract class EntityFunction : ContextFunction<Entity>() {
    override fun validateContext(context: IContext<*>): Boolean {
        return context.entity is Entity
    }
}