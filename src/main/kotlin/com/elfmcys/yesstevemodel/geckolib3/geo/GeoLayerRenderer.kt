package com.elfmcys.yesstevemodel.geckolib3.geo

import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.state.AvatarRenderState

abstract class GeoLayerRenderer<T : AnimatableEntity<*>> {
    abstract fun render(
        state: AvatarRenderState,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        packedLightIn: Int,
        entityLivingBaseIn: T,
        limbSwing: Float,
        limbSwingAmount: Float,
        partialTick: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    )
}