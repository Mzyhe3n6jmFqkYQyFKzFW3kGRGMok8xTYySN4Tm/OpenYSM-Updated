package com.elfmcys.yesstevemodel.client.renderer

import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.state.EntityRenderState
import java.util.IdentityHashMap
import java.util.Map

object PreviewEntityRegistry {

    fun interface SceneryRenderer {
        fun render(poseStack: PoseStack, bufferSource: MultiBufferSource, packedLight: Int)
    }

    data class Entry(
        val animatable: CustomPlayerEntity,
        val beforeEntity: SceneryRenderer? = null,
        val afterEntity: SceneryRenderer? = null,
        val poseYOffset: Float = 0.0f
    ) {
        fun animatable(): CustomPlayerEntity = animatable
        fun beforeEntity(): SceneryRenderer? = beforeEntity
        fun afterEntity(): SceneryRenderer? = afterEntity
        fun poseYOffset(): Float = poseYOffset
    }

    @JvmField
    val ENTRIES: MutableMap<EntityRenderState, Entry> = IdentityHashMap()

    @JvmStatic
    fun register(state: EntityRenderState, animatable: CustomPlayerEntity) {
        ENTRIES[state] = Entry(animatable, null, null, 0.0f)
    }

    @JvmStatic
    fun register(
        state: EntityRenderState,
        animatable: CustomPlayerEntity,
        beforeEntity: SceneryRenderer?,
        afterEntity: SceneryRenderer?
    ) {
        ENTRIES[state] = Entry(animatable, beforeEntity, afterEntity, 0.0f)
    }

    @JvmStatic
    fun register(
        state: EntityRenderState,
        animatable: CustomPlayerEntity,
        beforeEntity: SceneryRenderer?,
        afterEntity: SceneryRenderer?,
        poseYOffset: Float
    ) {
        ENTRIES[state] = Entry(animatable, beforeEntity, afterEntity, poseYOffset)
    }

    @JvmStatic
    fun get(state: EntityRenderState?): CustomPlayerEntity? {
        if (state == null) return null
        return ENTRIES[state]?.animatable
    }

    @JvmStatic
    fun getEntry(state: EntityRenderState?): Entry? {
        if (state == null) return null
        return ENTRIES[state]
    }

    @JvmStatic
    fun remove(state: EntityRenderState) {
        ENTRIES.remove(state)
    }
}