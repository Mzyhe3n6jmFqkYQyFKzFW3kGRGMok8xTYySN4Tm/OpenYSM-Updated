@file:Suppress("unused")

package com.elfmcys.yesstevemodel.geckolib3.util

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.entity.EntityRenderDispatcher
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.world.entity.Entity

object AnimationUtils {
    @JvmStatic
    fun convertTicksToSeconds(ticks: Float): Float = ticks / 20.0f

    @JvmStatic
    fun convertSecondsToTicks(seconds: Float): Float = seconds * 20.0f

    @JvmStatic
    fun <T : Entity> getRenderer(entity: T): EntityRenderer<in T, *> {
        val renderManager: EntityRenderDispatcher = Minecraft.getInstance().entityRenderDispatcher
        return renderManager.getRenderer(entity)
    }
}