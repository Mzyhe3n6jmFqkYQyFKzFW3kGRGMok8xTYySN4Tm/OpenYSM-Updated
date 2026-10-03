package com.elfmcys.yesstevemodel.access

import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.world.entity.Entity

interface IEntityRenderDispatcher {
    fun `ysm$getEntityForState`(state: EntityRenderState): Entity?
}