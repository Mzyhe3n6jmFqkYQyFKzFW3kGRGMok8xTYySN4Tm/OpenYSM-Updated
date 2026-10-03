package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.EntityFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.biome.Biome

class BiomeHasAnyTag : EntityFunction() {
    override fun eval(context: ExecutionContext<IContext<Entity>>, arguments: Function.ArgumentCollection): Any? {
        val entity: Entity = context.entity().entity()
        val biome: Holder<Biome> = entity.level().getBiome(entity.blockPosition())
        for (i in 0 until arguments.size()) {
            val id: Identifier = arguments.getResourceLocation(context, i) ?: return null
            val tag: TagKey<Biome> = TagKey.create(Registries.BIOME, id)
            if (biome.`is`(tag)) {
                return true
            }
        }
        return false
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size >= 1
    }
}