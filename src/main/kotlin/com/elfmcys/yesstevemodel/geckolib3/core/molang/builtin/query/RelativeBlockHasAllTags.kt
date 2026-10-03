package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query

import net.minecraft.core.registries.Registries
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.EntityFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import net.minecraft.resources.Identifier
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

open class RelativeBlockHasAllTags : EntityFunction() {
    open fun eval(context: ExecutionContext<IContext<Entity>>, arguments: ArgumentCollection): Any {
        var block: BlockState = MolangUtils.getRelativeBlockState(context, arguments)
        if (block == null) {
            return null
        }
        var i = 3
        while (i < arguments.size()) {
            var tagId: Identifier = arguments.getResourceLocation(context, i)
            if (tagId == null) {
                return null
            }
            var tag: TagKey<Block> = TagKey.create(Registries.BLOCK, tagId)
            if (!block.`is`(tag)) {
                return false
            }
            i++
        }
        return true
    }
    open fun validateArgumentSize(size: Int): Boolean {
        return size >= 4
    }
}