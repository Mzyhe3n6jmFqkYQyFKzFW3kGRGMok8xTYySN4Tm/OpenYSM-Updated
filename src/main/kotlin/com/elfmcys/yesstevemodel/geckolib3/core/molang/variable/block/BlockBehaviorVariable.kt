package com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.block

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.IValueEvaluator
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.LambdaVariable
import net.minecraft.world.level.block.state.BlockBehaviour

open class BlockBehaviorVariable(valueEvaluator: IValueEvaluator<*, IContext<BlockBehaviour>>) :
    LambdaVariable<BlockBehaviour>(valueEvaluator) {
    override fun validateContext(context: IContext<*>): Boolean {
        return context.entity is BlockBehaviour
    }
}