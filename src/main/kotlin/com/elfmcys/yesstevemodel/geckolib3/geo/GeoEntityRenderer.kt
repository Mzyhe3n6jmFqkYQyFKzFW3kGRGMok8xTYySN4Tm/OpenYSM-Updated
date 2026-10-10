package com.elfmcys.yesstevemodel.geckolib3.geo

import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.core.util.Color
import com.elfmcys.yesstevemodel.geckolib3.util.EModelRenderCycle
import com.elfmcys.yesstevemodel.geckolib3.util.IRenderCycle
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.math.Axis
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.entity.Entity
import org.joml.Matrix4f

abstract class GeoEntityRenderer<TEntity : Entity, T : AnimatableEntity<TEntity>>(
    context: EntityRendererProvider.Context
) : EntityRenderer<TEntity, EntityRenderState>(context), IGeoRenderer<T> {

    var worldMatrix: Matrix4f = Matrix4f()

    var modelMatrix: Matrix4f = Matrix4f()

    private var renderState: IRenderCycle = EModelRenderCycle.INITIAL

    var bufferSource: MultiBufferSource? = null

    open fun renderEntity(
        t: T,
        state: EntityRenderState,
        f: Float,
        f2: Float,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        i: Int
    ) {
        val event: AnimationEvent<*>? = t.processAnimation(f2)
        val minecraft = Minecraft.getInstance()
        val player = minecraft.player
        if (event != null && player != null) {
            val entity = t.entity
            val z = !entity.isInvisibleTo(player)
            val zShouldEntityAppearGlowing = minecraft.shouldEntityAppearGlowing(entity)
            val currentModel = t.currentModel ?: return
            val renderType = getRenderType(
                t.textureLocation,
                z,
                zShouldEntityAppearGlowing,
                currentModel.geoModel.isTranslucentTexture(0)
            )
            if (renderType != null && (z || zShouldEntityAppearGlowing)) {
                val color: Color = getRenderColor(t, f2, poseStack, multiBufferSource, null, i)
                worldMatrix = Matrix4f(poseStack.last().pose())
                currentModelRenderCycle = EModelRenderCycle.INITIAL
                poseStack.pushPose()
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - f))
                renderWithBoneAndRenderType(
                    currentModel,
                    t,
                    f2,
                    renderType,
                    poseStack,
                    multiBufferSource,
                    0,
                    null,
                    i,
                    packOverlayCoords(entity, 0.0f),
                    color.red / 255.0f,
                    color.green / 255.0f,
                    color.blue / 255.0f,
                    color.alpha / 255.0f
                )
                poseStack.popPose()
            }
        }
    }

    override fun createRenderState(): EntityRenderState = EntityRenderState()

    override fun renderEarly(
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
        modelMatrix = Matrix4f(poseStack.last().pose())
        super.renderEarly(
            animatable,
            poseStack,
            partialTick,
            bufferSource,
            buffer,
            packedLight,
            packedOverlayIn,
            red,
            green,
            blue,
            alpha
        )
    }

    override var currentModelRenderCycle: IRenderCycle
        get() = renderState
        set(value) {
            renderState = value
        }

    override var currentRTB: MultiBufferSource?
        get() = bufferSource
        set(value) {
            bufferSource = value
        }

    companion object {
        fun packOverlayCoords(entity: Entity, f: Float): Int =
            OverlayTexture.pack(OverlayTexture.u(f), OverlayTexture.v(false))
    }
}