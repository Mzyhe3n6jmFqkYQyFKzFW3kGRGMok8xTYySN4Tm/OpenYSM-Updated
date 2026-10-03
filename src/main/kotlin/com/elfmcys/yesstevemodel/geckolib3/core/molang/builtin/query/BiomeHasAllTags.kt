package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query

import net.minecraft.core.registries.Registries
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.EntityFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import net.minecraft.core.Holder
import net.minecraft.resources.Identifier
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.biome.Biome

open class BiomeHasAllTags : EntityFunction() {
    open fun eval(context: ExecutionContext<IContext<Entity>>, arguments: ArgumentCollection): Any {
        var entity: Entity = context.entity().entity()
        var biome: Holder<Biome> = entity.level().getBiome(entity.blockPosition())
        var i = 0
        while (i < arguments.size()) {
            var id: Identifier = arguments.getResourceLocation(context, i)
            if (id == null) {
                return null
            }
            var tag: TagKey<Biome> = TagKey.create(Registries.BIOME, id)
            if (!biome.`is`(tag)) {
                return false
            }
            i++
        }
        return true
    }
    open fun validateArgumentSize(size: Int): Boolean {
        return size >= 1
    }
}