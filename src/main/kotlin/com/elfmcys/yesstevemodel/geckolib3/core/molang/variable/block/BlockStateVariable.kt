package com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.block

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.IValueEvaluator
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.LambdaVariable
import net.minecraft.world.level.block.state.BlockState

open class BlockStateVariable(evaluator: IValueEvaluator<*, IContext<BlockState>>) :
    LambdaVariable<BlockState>(evaluator) {
    override fun validateContext(context: IContext<*>): Boolean {
        return context.entity() is BlockState
    }
}