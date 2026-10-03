package com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction
import net.minecraft.world.entity.Mob

abstract class MobEntityFunction : ContextFunction<Mob>() {
    override fun validateContext(context: IContext<*>): Boolean {
        return context.entity() is Mob
    }
}