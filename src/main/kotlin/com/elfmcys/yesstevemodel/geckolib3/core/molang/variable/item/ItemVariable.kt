package com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.item

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.IValueEvaluator
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.LambdaVariable
import net.minecraft.world.item.Item

open class ItemVariable(evaluator: IValueEvaluator<*, IContext<Item>>) :
    LambdaVariable<Item>(evaluator) {
    override fun validateContext(context: IContext<*>): Boolean = context.entity() is Item
}