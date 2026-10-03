package com.elfmcys.yesstevemodel.client.renderer

import com.google.common.collect.MapMaker
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.world.entity.Entity
import java.util.Map

object EntityRenderStateBindings {
    @JvmField
    val BINDINGS: MutableMap<EntityRenderState, Entity> = MapMaker().weakKeys().makeMap()

    @JvmStatic
    fun bind(state: EntityRenderState?, entity: Entity?) {
        if (state != null && entity != null) {
            BINDINGS[state] = entity
        }
    }

    @JvmStatic
    fun get(state: EntityRenderState?): Entity? {
        return if (state == null) null else BINDINGS[state]
    }
}