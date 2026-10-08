package com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.blocks

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction
import net.minecraft.world.level.block.state.BlockBehaviour

abstract class AbstractBlockFunction : ContextFunction<BlockBehaviour>() {
    override fun validateContext(context: IContext<*>): Boolean {
        return context.entity is BlockBehaviour
    }
}