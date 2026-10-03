package com.elfmcys.yesstevemodel.client.renderer.layer

import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoLayerRenderer
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel
import com.elfmcys.yesstevemodel.geckolib3.util.RenderUtils
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.model.animal.parrot.ParrotModel
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.ParrotRenderer
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.entity.state.ParrotRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.entity.animal.parrot.Parrot
import net.minecraft.world.entity.player.Player

open class CustomPlayerParrotLayer(context: EntityRendererProvider.Context) : GeoLayerRenderer<CustomPlayerEntity>() {
    private val parrotModel: ParrotModel = ParrotModel(context.bakeLayer(ModelLayers.PARROT))
    private val parrotState: ParrotRenderState = ParrotRenderState().apply {
        pose = ParrotModel.Pose.ON_SHOULDER
    }

    override fun render(
        state: AvatarRenderState,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLightIn: Int,
        entityLivingBaseIn: CustomPlayerEntity,
        limbSwing: Float,
        limbSwingAmount: Float,
        partialTick: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        val player: Player = entityLivingBaseIn.getEntity()
        val model: AnimatedGeoModel? = entityLivingBaseIn.getCurrentModel()
        if (model == null) {
            return
        }
        if (!model.leftShoulderBones().isEmpty()) {
            renderParrot(poseStack, state, bufferSource, model, packedLightIn, player, limbSwing, limbSwingAmount, netHeadYaw, headPitch, true)
        }
        if (!model.rightShoulderBones().isEmpty()) {
            renderParrot(poseStack, state, bufferSource, model, packedLightIn, player, limbSwing, limbSwingAmount, netHeadYaw, headPitch, false)
        }
    }

    open fun renderParrot(
        poseStack: PoseStack,
        state: AvatarRenderState,
        bufferSource: MultiBufferSource,
        model: AnimatedGeoModel,
        packedLightIn: Int,
        player: Player,
        limbSwing: Float,
        limbSwingAmount: Float,
        netHeadYaw: Float,
        headPitch: Float,
        isLeftShoulder: Boolean
    ) {
        val variant: Parrot.Variant = (if (isLeftShoulder) state.parrotOnLeftShoulder else state.parrotOnRightShoulder) ?: return
        poseStack.pushPose()
        applyParrotTransform(poseStack, model, isLeftShoulder)
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f))
        renderOnShoulder(poseStack, bufferSource, packedLightIn, state, variant, headPitch, netHeadYaw, isLeftShoulder)
        poseStack.popPose()
    }

    open fun renderOnShoulder(
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int,
        state: AvatarRenderState,
        variant: Parrot.Variant,
        pitch: Float,
        yaw: Float,
        leftShoulder: Boolean
    ) {
        poseStack.pushPose()
        poseStack.translate(0.0f, if (state.isCrouching) -1.3f else -1.5f, 0.0f)
        parrotState.ageInTicks = state.ageInTicks
        parrotState.walkAnimationPos = state.walkAnimationPos
        parrotState.walkAnimationSpeed = state.walkAnimationSpeed
        parrotState.yRot = yaw
        parrotState.xRot = pitch
        parrotModel.setupAnim(parrotState)
        parrotModel.renderToBuffer(
            poseStack,
            bufferSource.getBuffer(parrotModel.renderType(ParrotRenderer.getVariantTexture(variant))),
            packedLight,
            OverlayTexture.NO_OVERLAY
        )
        poseStack.popPose()
    }

    open fun applyParrotTransform(poseStack: PoseStack, model: AnimatedGeoModel, isLeftShoulder: Boolean) {
        if (isLeftShoulder) {
            RenderUtils.prepMatrixForLocator(poseStack, model.leftShoulderBones())
        } else {
            RenderUtils.prepMatrixForLocator(poseStack, model.rightShoulderBones())
        }
    }
}