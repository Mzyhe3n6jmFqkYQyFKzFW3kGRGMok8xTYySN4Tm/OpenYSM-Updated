package com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.item

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction
import net.minecraft.world.item.ItemStack

abstract class ItemStackFunction : ContextFunction<ItemStack>() {
    override fun validateContext(context: IContext<*>): Boolean {
        return context.entity is ItemStack
    }
}