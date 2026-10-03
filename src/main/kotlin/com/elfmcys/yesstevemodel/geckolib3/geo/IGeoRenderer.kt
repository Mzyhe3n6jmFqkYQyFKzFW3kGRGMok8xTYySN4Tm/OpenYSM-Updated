package com.elfmcys.yesstevemodel.geckolib3.geo

import com.elfmcys.yesstevemodel.client.renderer.CustomEntityTranslucentRenderType
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.util.Color
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel
import com.elfmcys.yesstevemodel.geckolib3.util.EModelRenderCycle
import com.elfmcys.yesstevemodel.geckolib3.util.IRenderCycle
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.resources.Identifier

interface IGeoRenderer<T : AnimatableEntity<*>> {
    fun getCurrentRTB(): MultiBufferSource?

    fun setCurrentRTB(bufferSource: MultiBufferSource?) {
    }

    fun renderWithBone(
        model: AnimatedGeoModel,
        animatable: T,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource?,
        vertexConsumer: VertexConsumer?,
        packedLight: Int,
        packedOverlayIn: Int,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ) {
        setCurrentRTB(bufferSource)
        renderEarly(animatable, poseStack, partialTick, bufferSource, vertexConsumer, packedLight, packedOverlayIn, red, green, blue, alpha)
        renderLate(animatable, poseStack, partialTick, bufferSource, vertexConsumer, packedLight, packedOverlayIn, red, green, blue, alpha)
    }

    fun renderWithBoneAndRenderType(
        model: AnimatedGeoModel,
        animatable: T,
        partialTick: Float,
        renderType: RenderType,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource?,
        i: Int,
        vertexConsumer: VertexConsumer?,
        i2: Int,
        i3: Int,
        f2: Float,
        f3: Float,
        f4: Float,
        f5: Float
    ) {
        val consumer = vertexConsumer ?: bufferSource?.getBuffer(renderType)
        animatable.resetAnimationState()
        val tex = animatable.getTextureLocation()
        if (consumer != null) {
            NativeModelRenderer.renderMesh(consumer, poseStack.last(), model.getGeoModel(), model.getMatrixData(), model.getAbsPivotData(), i, 0, i2, i3, f2, f3, f4, f5, tex)
        }
        setCurrentModelRenderCycle(EModelRenderCycle.REPEATED)
    }

    fun renderEarly(
        animatable: T,
        poseStack: PoseStack,
        partialTick: Float,
        bufferSource: MultiBufferSource?,
        buffer: VertexConsumer?,
        packedLight: Int,
        packedOverlayIn: Int,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ) {
        if (getCurrentModelRenderCycle() == EModelRenderCycle.INITIAL) {
            val width = animatable.getHeightScale()
            val height = animatable.getWidthScale()
            poseStack.scale(width, height, width)
        }
    }

    fun renderLate(
        animatable: T,
        poseStack: PoseStack,
        partialTick: Float,
        bufferSource: MultiBufferSource?,
        buffer: VertexConsumer?,
        packedLight: Int,
        packedOverlayIn: Int,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float
    ) {
    }

    fun getRenderType(identifier: Identifier, z: Boolean, z2: Boolean, z3: Boolean): RenderType? {
        if (z) {
            if (z3) {
                return CustomEntityTranslucentRenderType.get(identifier)
            }
            return RenderTypes.entityCutoutNoCull(identifier)
        }
        if (z2) {
            return RenderTypes.outline(identifier)
        }
        return null
    }

    fun getRenderColor(
        animatable: T,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource?,
        buffer: VertexConsumer?,
        packedLight: Int
    ): Color {
        return Color.WHITE
    }

    fun getCurrentModelRenderCycle(): IRenderCycle {
        return EModelRenderCycle.INITIAL
    }

    fun setCurrentModelRenderCycle(cycle: IRenderCycle) {
    }
}