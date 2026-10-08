package com.elfmcys.yesstevemodel.client.renderer.layer

import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoLayerRenderer
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel
import com.elfmcys.yesstevemodel.geckolib3.util.RenderUtils
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.model.`object`.equipment.ElytraModel
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.PlayerModelPart
import rip.ysm.compat.cosmeticarmorreworked.CosmeticArmorHelper

open class CustomPlayerElytraLayer(context: EntityRendererProvider.Context) : GeoLayerRenderer<CustomPlayerEntity>() {
    private val elytraModel: ElytraModel = ElytraModel(context.modelSet.bakeLayer(ModelLayers.ELYTRA))

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
        val entity = entityLivingBaseIn.entity
        val stack = CosmeticArmorHelper.getElytraItem(entity)
        val animatedGeoModel = entityLivingBaseIn.currentModel
        if (!stack.isEmpty && animatedGeoModel != null && animatedGeoModel.elytraBones
                .isNotEmpty() && entity is AbstractClientPlayer
        ) {
            val skin = entity.skin
            val cloakTextureLocation = skin.elytra()?.texturePath()
                ?: if (skin.cape() != null && entity.isModelPartShown(PlayerModelPart.CAPE)) skin.cape()?.texturePath()
                    ?: WINGS_LOCATION else WINGS_LOCATION

            poseStack.pushPose()
            renderElytra(poseStack, animatedGeoModel)
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f))
            elytraModel.setupAnim(state)
            elytraModel.renderToBuffer(
                poseStack,
                ItemRenderer.getFoilBuffer(
                    multiBufferSource,
                    RenderTypes.armorCutoutNoCull(cloakTextureLocation),
                    false,
                    stack.hasFoil()
                ),
                packedLightIn,
                OverlayTexture.NO_OVERLAY,
                -1
            )
            poseStack.popPose()
        }
    }

    open fun renderElytra(poseStack: PoseStack, model: AnimatedGeoModel) {
        RenderUtils.prepMatrixForLocator(poseStack, model.elytraBones)
    }

    companion object {
        private val WINGS_LOCATION: Identifier = Identifier.parse("textures/entity/equipment/wings/elytra.png")
    }
}