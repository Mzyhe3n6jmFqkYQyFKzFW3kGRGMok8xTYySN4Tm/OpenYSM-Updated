package com.elfmcys.yesstevemodel.client.renderer

import com.google.common.collect.MapMaker
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.world.entity.Entity

object EntityRenderStateBindings {
    val BINDINGS: MutableMap<EntityRenderState, Entity> = MapMaker().weakKeys().makeMap()

    @JvmStatic
    fun bind(state: EntityRenderState?, entity: Entity?) {
        if (state != null && entity != null) BINDINGS[state] = entity
    }

    @JvmStatic
    operator fun get(state: EntityRenderState?): Entity? = if (state == null) null else BINDINGS[state]
}