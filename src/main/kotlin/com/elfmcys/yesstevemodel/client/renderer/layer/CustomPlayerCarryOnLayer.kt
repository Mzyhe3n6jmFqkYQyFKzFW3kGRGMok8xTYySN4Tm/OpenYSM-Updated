package com.elfmcys.yesstevemodel.client.renderer.layer

import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.client.renderer.RenderContext
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoLayerRenderer
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import rip.ysm.compat.carryon.CarryOnCompat
import rip.ysm.compat.carryon.CarryOnDataHelper
import rip.ysm.compat.carryon.CarryOnRenderer

open class CustomPlayerCarryOnLayer : GeoLayerRenderer<CustomPlayerEntity>() {
    override fun render(
        state: AvatarRenderState,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        packedLightIn: Int,
        entityLivingBaseIn: CustomPlayerEntity,
        limbSwing: Float,
        limbSwingAmount: Float,
        partialTick: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        if (!CarryOnCompat.isLoaded()) return
        val entity = entityLivingBaseIn.entity as LivingEntity
        if (entity !is Player) return
        if (!CarryOnDataHelper.isPlayerCarrying(entity)) return
        val collector = RenderContext.collector() ?: return
        poseStack.pushPose()
        poseStack.scale(-1.0f, -1.0f, 1.0f)
        poseStack.translate(0.0, -1.501, 0.0)
        CarryOnRenderer.render(entity, poseStack, packedLightIn, partialTick, collector)
        poseStack.popPose()
    }
}