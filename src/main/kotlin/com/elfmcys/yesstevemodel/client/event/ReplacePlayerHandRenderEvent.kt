package com.elfmcys.yesstevemodel.client.event

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.client.renderer.RendererManager
import com.elfmcys.yesstevemodel.config.GeneralConfig
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.world.entity.HumanoidArm
import net.minecraft.world.entity.player.Player

object ReplacePlayerHandRenderEvent {
    @JvmStatic
    fun onRenderArm(
        player: Player,
        arm: HumanoidArm,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int
    ): Boolean {
        if (!YesSteveModel.isAvailable() || GeneralConfig.DISABLE_SELF_MODEL.get() || GeneralConfig.DISABLE_SELF_HANDS.get())
            return false
        if (player !is LocalPlayer) return false
        var cancelled = false
        PlayerCapability[player]?.let { cap ->
            if (!cap.isModelActive) return@let
            val context: ModelAssembly? = cap.modelAssembly
            if (context == null || !hasArmBone(arm, context.animationBundle.armModel)) return@let
            RendererManager.getHandRenderer().renderHandItem(
                player,
                context,
                cap,
                arm,
                poseStack,
                bufferSource,
                packedLight,
                Minecraft.getInstance().deltaTracker.getGameTimeDeltaPartialTick(false)
            )
            cancelled = true
        }
        return cancelled
    }

    private fun hasArmBone(humanoidArm: HumanoidArm, meshData: GeoModel): Boolean {
        return if (humanoidArm == HumanoidArm.LEFT) {
            meshData.hasCustomLeftHand
        } else {
            meshData.hasCustomRightHand
        }
    }
}
