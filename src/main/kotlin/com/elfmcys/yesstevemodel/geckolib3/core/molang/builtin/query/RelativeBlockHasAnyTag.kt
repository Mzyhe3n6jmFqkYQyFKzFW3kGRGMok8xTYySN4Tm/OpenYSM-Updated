package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.EntityFunction
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.block.state.BlockState

class RelativeBlockHasAnyTag : EntityFunction() {
    override fun eval(context: ExecutionContext<IContext<Entity>>, arguments: Function.ArgumentCollection): Any? {
        val block: BlockState = MolangUtils.getRelativeBlockState(context, arguments) ?: return null
        for (i in 3 until arguments.size()) {
            val key: Identifier = arguments.getResourceLocation(context, i) ?: return null
            if (block.`is`(TagKey.create(Registries.BLOCK, key))) {
                return true
            }
        }
        return false
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size >= 4
    }
}