package com.elfmcys.yesstevemodel.client.renderer

import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.resources.Identifier

object CustomEntityTranslucentRenderType {
    @JvmStatic
    fun get(identifier: Identifier): RenderType {
        return RenderTypes.entityTranslucent(identifier)
    }
}