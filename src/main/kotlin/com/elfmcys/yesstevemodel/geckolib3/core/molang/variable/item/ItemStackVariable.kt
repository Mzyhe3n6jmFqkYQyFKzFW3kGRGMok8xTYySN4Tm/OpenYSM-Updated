package com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.item

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.IValueEvaluator
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.LambdaVariable
import net.minecraft.world.item.ItemStack

open class ItemStackVariable(evaluator: IValueEvaluator<*, IContext<ItemStack>>) :
    LambdaVariable<ItemStack>(evaluator) {
    override fun validateContext(context: IContext<*>): Boolean = context.entity is ItemStack
}