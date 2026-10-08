package com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.block

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.IValueEvaluator
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.LambdaVariable
import net.minecraft.world.level.block.Block

open class BlockVariable(evaluator: IValueEvaluator<*, IContext<Block>>) :
    LambdaVariable<Block>(evaluator) {
    override fun validateContext(context: IContext<*>): Boolean {
        return context.entity is Block
    }
}