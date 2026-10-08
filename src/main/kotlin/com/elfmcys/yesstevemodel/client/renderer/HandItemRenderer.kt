package com.elfmcys.yesstevemodel.client.renderer

import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.client.entity.PlayerGeoEntity
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.event.api.SpecialPlayerRenderEvent
import com.elfmcys.yesstevemodel.geckolib3.geo.LayerTypeConstants
import com.elfmcys.yesstevemodel.geckolib3.geo.NativeModelRenderer
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.HumanoidArm

open class HandItemRenderer {
    private var geoModel: PlayerGeoEntity? = null

    open fun renderHandItem(
        localPlayer: LocalPlayer,
        modelAssembly: ModelAssembly,
        capability: PlayerCapability,
        arm: HumanoidArm,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int,
        partialTick: Float
    ) {
        val currentGeoModel = geoModel
        val currentOrNewGeoModel = if (currentGeoModel == null || currentGeoModel.entity != localPlayer) {
            val newGeoModel = PlayerGeoEntity(localPlayer, capability)
            geoModel = newGeoModel
            newGeoModel
        } else {
            currentGeoModel
        }
        currentOrNewGeoModel.tickModel()
        if (currentOrNewGeoModel.processAnimation(partialTick) == null) {
            return
        }
        val model: AnimatedGeoModel = currentOrNewGeoModel.currentModel ?: return
        val event = SpecialPlayerRenderEvent(localPlayer, capability, capability.modelId)
        if (SpecialPlayerRenderEvent.post(event).isFalse()) {
            return
        }
        val textureLocation: Identifier = event.textureLocation ?: capability.textureLocation
        val textureIndex: Int = if (event.textureLocation == null) capability.getTextureIndex() else 0
        val buffer: VertexConsumer = bufferSource.getBuffer(CustomEntityTranslucentRenderType.get(textureLocation))
        val renderPartMask: Int =
            if (arm == HumanoidArm.LEFT) LayerTypeConstants.TYPE_LEFT else LayerTypeConstants.TYPE_RIGHT
        poseStack.pushPose()
        if (arm == HumanoidArm.LEFT) {
            poseStack.translate(0.25, 1.8, 0.0)
        } else {
            poseStack.translate(-0.25, 1.8, 0.0)
        }
        poseStack.scale(-1.0f, -1.0f, 1.0f)
        NativeModelRenderer.renderMesh(
            buffer,
            poseStack.last(),
            model.getGeoModel(),
            model.getMatrixData(),
            model.getAbsPivotData(),
            textureIndex,
            renderPartMask,
            packedLight,
            OverlayTexture.NO_OVERLAY,
            1.0f,
            1.0f,
            1.0f,
            1.0f
        )
        poseStack.popPose()
    }
}