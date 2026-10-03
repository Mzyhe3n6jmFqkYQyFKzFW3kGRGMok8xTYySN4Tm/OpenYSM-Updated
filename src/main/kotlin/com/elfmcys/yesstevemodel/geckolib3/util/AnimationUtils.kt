package com.elfmcys.yesstevemodel.geckolib3.util

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.entity.EntityRenderDispatcher
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.world.entity.Entity

@Suppress("UNCHECKED_CAST")
object AnimationUtils {
    @JvmStatic
    fun convertTicksToSeconds(ticks: Float): Float {
        return ticks / 20.0f
    }

    @JvmStatic
    fun convertSecondsToTicks(seconds: Float): Float {
        return seconds * 20.0f
    }

    @JvmStatic
    fun <T : Entity> getRenderer(entity: T): EntityRenderer<T, *>? {
        val renderManager: EntityRenderDispatcher = Minecraft.getInstance().entityRenderDispatcher
        return renderManager.getRenderer(entity) as? EntityRenderer<T, *>
    }
}