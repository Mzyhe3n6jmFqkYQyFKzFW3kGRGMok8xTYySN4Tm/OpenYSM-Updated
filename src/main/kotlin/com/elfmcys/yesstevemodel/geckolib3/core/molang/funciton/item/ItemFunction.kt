package com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.item

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction
import net.minecraft.world.item.Item

abstract class ItemFunction : ContextFunction<Item>() {
    override fun validateContext(context: IContext<*>): Boolean {
        return context.entity() is Item
    }
}