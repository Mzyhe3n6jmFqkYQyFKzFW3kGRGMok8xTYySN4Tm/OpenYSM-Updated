package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.EntityFunction
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function.ArgumentCollection
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.block.state.BlockState

class RelativeBlockNameAny : EntityFunction() {
    override fun eval(context: ExecutionContext<IContext<Entity>>, arguments: ArgumentCollection): Any? {
        val blockState: BlockState = MolangUtils.getRelativeBlockState(context, arguments) ?: return null
        val key: Identifier = BuiltInRegistries.BLOCK.getKey(blockState.block) ?: return null
        for (i in 3 until arguments.size()) {
            if (key == arguments.getResourceLocation(context, i)) {
                return true
            }
        }
        return false
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size > 3
    }
}