package com.elfmcys.yesstevemodel.client.event

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.client.renderer.CustomEntityTranslucentRenderType
import com.elfmcys.yesstevemodel.client.renderer.CustomPlayerRenderer
import com.elfmcys.yesstevemodel.client.renderer.RendererManager
import com.elfmcys.yesstevemodel.config.GeneralConfig
import com.elfmcys.yesstevemodel.event.api.SpecialPlayerRenderEvent
import com.elfmcys.yesstevemodel.geckolib3.geo.NativeModelRenderer
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.Minecraft
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.Identifier
import net.minecraft.util.Mth
import net.minecraft.world.entity.player.Player
import kotlin.math.abs

object RenderFirstPlayerBackground {
    // 因为RenderHandEvent可有几率会渲染多次，所以为了避免多次渲染，这样设计
    @JvmField
    var currentFrameRendered: Boolean = false

    @JvmStatic
    fun resetFrame() {
        currentFrameRendered = false
    }

    @JvmStatic
    fun onRenderHand(poseStack: PoseStack, multiBufferSource: MultiBufferSource, packedLight: Int, partialTick: Float) {
        if (!YesSteveModel.isAvailable()) {
            return
        }
        if (GeneralConfig.DISABLE_SELF_MODEL.get() || GeneralConfig.DISABLE_SELF_HANDS.get()) {
            return
        }
        val player = Minecraft.getInstance().player ?: return
        if (currentFrameRendered) {
            return
        }
        currentFrameRendered = true
        PlayerCapability[player]?.let { cap ->
            if (!cap.isModelActive()) {
                return@let
            }
            val modelId = cap.getModelId()
            val modelAssembly: ModelAssembly? = cap.getModelAssembly()
            if (modelAssembly == null || !modelAssembly.getAnimationBundle().getArmModel().hasCustomLimbs) {
                return@let
            }
            val instance: CustomPlayerRenderer? = RendererManager.getPlayerRenderer()
            val result = SpecialPlayerRenderEvent.post(SpecialPlayerRenderEvent(player, cap, modelId))
            if (result.isFalse()) {
                return@let
            }
            val resourceLocationB_: Identifier = cap.getTextureLocation()
            val textureIndex = cap.getTextureIndex()
            val buffer = multiBufferSource.getBuffer(CustomEntityTranslucentRenderType.get(resourceLocationB_))
            if (instance != null) {
                poseStack.pushPose()
                if (Minecraft.getInstance().options.bobView().get()) {
                    applyHandTransform(poseStack, partialTick, player)
                }
                poseStack.translate(0.0, -1.5, 0.0)
                NativeModelRenderer.renderMesh(
                    buffer,
                    poseStack,
                    modelAssembly.getAnimationBundle().getArmModel(),
                    modelAssembly.getAnimationBundle().getArmModel().boneTransformData,
                    FloatArray(0),
                    textureIndex,
                    3,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    1.0f,
                    1.0f,
                    1.0f,
                    1.0f,
                    resourceLocationB_
                )
                poseStack.popPose()
            }
        }
    }

    @JvmStatic
    private fun applyHandTransform(poseStack: PoseStack, partialTick: Float, player: Player) {
        val ap = player as AbstractClientPlayer
        val walkPhase: Float = ap.avatarState().getBackwardsInterpolatedWalkDistance(partialTick)
        val fLerp: Float = ap.avatarState().getInterpolatedBob(partialTick)
        val tx: Float = (-Mth.sin(walkPhase * 3.1415927f)) * fLerp * 0.5f
        val ty: Float = abs(Mth.cos(walkPhase * 3.1415927f) * fLerp)
        poseStack.translate(tx.toDouble(), ty.toDouble(), 0.0)
        poseStack.mulPose(Axis.ZN.rotationDegrees(Mth.sin(walkPhase * 3.1415927f) * fLerp * 3.0f))
        poseStack.mulPose(Axis.XN.rotationDegrees(abs(Mth.cos((walkPhase * 3.1415927f) - 0.2f) * fLerp) * 5.0f))
    }
}
