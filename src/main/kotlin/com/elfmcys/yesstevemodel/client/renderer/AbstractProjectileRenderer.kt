package com.elfmcys.yesstevemodel.client.renderer

import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.core.util.Color
import com.elfmcys.yesstevemodel.geckolib3.geo.IGeoRenderer
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel
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
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.projectile.Projectile
import org.joml.Matrix4f

abstract class AbstractProjectileRenderer<TEntity : Projectile, T : AnimatableEntity<TEntity>, S : EntityRenderState>(
    context: EntityRendererProvider.Context
) : EntityRenderer<TEntity, S>(context), IGeoRenderer<T> {

    @JvmField
    var modelViewMatrix: Matrix4f = Matrix4f()

    @JvmField
    var projectionMatrix: Matrix4f = Matrix4f()

    private var renderState: IRenderCycle = EModelRenderCycle.INITIAL

    @JvmField
    var bufferSource: MultiBufferSource? = null

    open fun render(
        animatable: T,
        state: S,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int
    ) {
        val event: AnimationEvent<*>? = animatable.processAnimation(partialTick)
        val minecraft = Minecraft.getInstance()
        val player = minecraft.player
        val model: AnimatedGeoModel? = animatable.currentModel
        if (event != null && player != null && model != null) {
            val projectile = animatable.entity
            val isVisible = !projectile.isInvisibleTo(player)
            val zShouldEntityAppearGlowing = minecraft.shouldEntityAppearGlowing(projectile)
            val renderType: RenderType? = getRenderType(
                animatable.textureLocation,
                isVisible,
                zShouldEntityAppearGlowing,
                model.getGeoModel().isTranslucentTexture(0)
            )
            if (renderType != null && (isVisible || zShouldEntityAppearGlowing)) {
                val color: Color = getRenderColor(animatable, partialTick, poseStack, bufferSource, null, packedLight)
                modelViewMatrix = Matrix4f(poseStack.last().pose())
                currentModelRenderCycle = EModelRenderCycle.INITIAL
                poseStack.pushPose()
                poseStack.mulPose(
                    Axis.YP.rotationDegrees(
                        Mth.lerp(
                            partialTick,
                            projectile.yRotO,
                            projectile.yRot
                        ) - 90.0f
                    )
                )
                poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, projectile.xRotO, projectile.xRot)))
                renderWithBoneAndRenderType(
                    model,
                    animatable,
                    partialTick,
                    renderType,
                    poseStack,
                    bufferSource,
                    0,
                    null,
                    packedLight,
                    getPackedLight(projectile, 0.0f),
                    color.getRed() / 255.0f,
                    color.getGreen() / 255.0f,
                    color.getBlue() / 255.0f,
                    color.getAlpha() / 255.0f
                )
                poseStack.popPose()
            }
        }
    }

    abstract override fun createRenderState(): S

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
        projectionMatrix = Matrix4f(poseStack.last().pose())
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
        @JvmStatic
        fun getPackedLight(entity: Entity, u: Float): Int {
            return OverlayTexture.pack(OverlayTexture.u(u), OverlayTexture.v(false))
        }
    }
}