package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query

import net.minecraft.tags.TagKey
import net.minecraft.core.registries.Registries
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.EntityFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.block.state.BlockState

open class RelativeBlockHasAnyTag : EntityFunction() {
    open fun eval(context: ExecutionContext<IContext<Entity>>, arguments: ArgumentCollection): Any {
        var block: BlockState = MolangUtils.getRelativeBlockState(context, arguments)
        if (block == null) {
            return null
        }
        var i = 3
        while (i < arguments.size()) {
            var key: Identifier = arguments.getResourceLocation(context, i)
            if (key == null) {
                return null
            }
            if (block.`is`(TagKey.create(Registries.BLOCK, key))) {
                return true
            }
            i++
        }
        return false
    }
    open fun validateArgumentSize(size: Int): Boolean {
        return size >= 4
    }
}