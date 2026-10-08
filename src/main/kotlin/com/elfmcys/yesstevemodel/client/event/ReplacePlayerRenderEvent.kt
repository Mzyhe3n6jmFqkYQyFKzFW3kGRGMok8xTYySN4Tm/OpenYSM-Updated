package com.elfmcys.yesstevemodel.client.event

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer
import com.elfmcys.yesstevemodel.client.renderer.RenderContext
import com.elfmcys.yesstevemodel.client.renderer.RendererManager
import com.elfmcys.yesstevemodel.config.GeneralConfig
import com.elfmcys.yesstevemodel.util.CameraUtil
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.state.CameraRenderState
import net.minecraft.world.entity.player.Player
import rip.ysm.compat.firstperson.FirstPersonCompat
import rip.ysm.compat.oculus.OculusCompat
import rip.ysm.compat.playeranimator.PlayerAnimatorCompat
import rip.ysm.compat.realcamera.RealCameraCompat

object ReplacePlayerRenderEvent {
    @JvmStatic
    fun onRenderPlayerPre(
        entity: Player,
        renderState: AvatarRenderState,
        partialTick: Float,
        poseStack: PoseStack,
        collector: SubmitNodeCollector,
        cameraState: CameraRenderState
    ): Boolean {
        if (!YesSteveModel.isAvailable) return false
        val localPlayer = Minecraft.getInstance().player
        if (entity == localPlayer && GeneralConfig.DISABLE_SELF_MODEL.get()) return false
        if ((entity != localPlayer && GeneralConfig.DISABLE_OTHER_MODEL.get()) || entity.isSpectator) return false
        var cancelled = false
        PlayerCapability[entity]?.let { cap ->
            if (cap.isModelActive) {
                if (!CameraUtil.isFirstPerson(cap) ||
                    FirstPersonCompat.isFirstPersonActive() ||
                    RealCameraCompat.isActive() ||
                    GeneralConfig.DISABLE_EXTERNAL_FP_ANIM.get() ||
                    (localPlayer == null || !PlayerAnimatorCompat.isPlayerAnimated(localPlayer))
                ) {
                    cancelled = true
                    val bufferSource = Minecraft.getInstance().renderBuffers().bufferSource()
                    RenderContext.enter(collector, cameraState)
                    try {
                        val packedLight = if (ModelPreviewRenderer.isPreview) {
                            LightTexture.FULL_BRIGHT
                        } else {
                            renderState.lightCoords
                        }
                        RendererManager.getPlayerRenderer().render(
                            entity,
                            renderState,
                            entity.yRot,
                            if (ModelPreviewRenderer.isPreview) 1.0f else partialTick,
                            poseStack,
                            bufferSource,
                            packedLight
                        )
                        if (OculusCompat.isRenderingShadowPass()) {
                            bufferSource.endBatch()
                        }
                    } finally {
                        RenderContext.exit()
                    }
                }
            }
        }
        return cancelled
    }
}
