package com.elfmcys.yesstevemodel.geckolib3.geo

import com.elfmcys.yesstevemodel.capability.VehicleCapability
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer
import com.elfmcys.yesstevemodel.client.renderer.RenderContext
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.extended.LivingEntityRendererAccessor
import com.elfmcys.yesstevemodel.geckolib3.model.provider.data.EntityModelData
import com.elfmcys.yesstevemodel.geckolib3.util.EModelRenderCycle
import com.elfmcys.yesstevemodel.geckolib3.util.IRenderCycle
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.client.Minecraft
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.model.player.PlayerModel
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.HumanoidMobRenderer
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import org.joml.Matrix4f
import org.joml.Quaternionf
import rip.ysm.api.client.RenderLivingBridge

abstract class GeoReplacedEntityRenderer<TEntity : Player, T : LivingAnimatable<TEntity>, S : AvatarRenderState>(
    context: EntityRendererProvider.Context
) : LivingEntityRenderer<TEntity, S, PlayerModel>(
    context,
    PlayerModel(context.bakeLayer(ModelLayers.PLAYER_SLIM), true),
    0.5f
), IGeoRenderer<T> {
    @JvmField
    val layerRenderers: MutableList<GeoLayerRenderer<T>> = ObjectArrayList()

    @JvmField
    var dispatchedMat: Matrix4f = Matrix4f()

    @JvmField
    var renderEarlyMat: Matrix4f = Matrix4f()

    @JvmField
    var rtb: MultiBufferSource? = null

    private var currentModelRenderCycle: IRenderCycle = EModelRenderCycle.INITIAL

    override fun getCurrentModelRenderCycle(): IRenderCycle = currentModelRenderCycle

    override fun setCurrentModelRenderCycle(cycle: IRenderCycle) {
        currentModelRenderCycle = cycle
    }

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
        renderEarlyMat.set(poseStack.last().pose())
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

    open fun renderEntity(
        t: T,
        state: S,
        entityYaw: Float,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int
    ) {
        renderEntityWithTexture(t, state, null, entityYaw, partialTick, poseStack, bufferSource, packedLight)
    }

    open fun renderEntityWithTexture(
        t: T,
        state: S,
        identifier: Identifier?,
        entityYaw: Float,
        partialTick: Float,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        packedLight: Int
    ) {
        val entity = t.entity
        if (RenderLivingBridge.firePre(entity, this, partialTick, poseStack, multiBufferSource, packedLight)) {
            return
        }

        var savedYBodyRot = 0.0f
        var savedYBodyRotO = 0.0f
        var savedYHeadRot = 0.0f
        var savedYHeadRotO = 0.0f
        var savedYRot = 0.0f
        var savedYRotO = 0.0f
        var savedXRot = 0.0f
        var savedXRotO = 0.0f
        val syncRotationsForPreview = ModelPreviewRenderer.isPreview
        if (syncRotationsForPreview) {
            savedYBodyRot = entity.yBodyRot
            savedYBodyRotO = entity.yBodyRotO
            savedYHeadRot = entity.yHeadRot
            savedYHeadRotO = entity.yHeadRotO
            savedYRot = entity.yRot
            savedYRotO = entity.yRotO
            savedXRot = entity.xRot
            savedXRotO = entity.xRotO
            val bodyRot = state.bodyRot
            val headYaw = state.bodyRot + state.yRot
            entity.yBodyRot = bodyRot
            entity.yBodyRotO = bodyRot
            entity.yHeadRot = headYaw
            entity.yHeadRotO = headYaw
            entity.yRot = headYaw
            entity.yRotO = headYaw
            entity.xRot = state.xRot
            entity.xRotO = state.xRot
        }

        val event: AnimationEvent<*>?
        try {
            event = t.processAnimation(partialTick)
        } finally {
            if (syncRotationsForPreview) {
                entity.yBodyRot = savedYBodyRot
                entity.yBodyRotO = savedYBodyRotO
                entity.yHeadRot = savedYHeadRot
                entity.yHeadRotO = savedYHeadRotO
                entity.yRot = savedYRot
                entity.yRotO = savedYRotO
                entity.xRot = savedXRot
                entity.xRotO = savedXRotO
            }
        }

        val minecraft = Minecraft.getInstance()
        val player = minecraft.player
        if (event != null && player != null) {
            val modelData = event.getModelData()
            dispatchedMat.set(poseStack.last().pose())
            setCurrentModelRenderCycle(EModelRenderCycle.INITIAL)
            poseStack.pushPose()
            if (entity.pose == Pose.SLEEPING) {
                val bedOrientation = entity.bedOrientation
                if (bedOrientation != null) {
                    val eyeHeight = entity.getEyeHeight(Pose.STANDING) - 0.1f
                    poseStack.translate(-bedOrientation.stepX * eyeHeight, 0.0f, -bedOrientation.stepZ * eyeHeight)
                }
            }
            setupRotations(entity, state, poseStack, modelData.lerpedAge, modelData.lerpBodyRot, partialTick, 1.0f)
            val vehicle = entity.vehicle
            if (vehicle != null) {
                val cap = VehicleCapability[vehicle]
                val vector3f = cap?.expressionOffset
                if (vector3f != null) {
                    poseStack.mulPose(Quaternionf().rotateZYX(vector3f.z, 0.0f, vector3f.x).invert())
                }
            }
            preRenderCallback(entity, poseStack, partialTick)
            poseStack.translate(0.0f, 0.01f, 0.0f)
            val animatedGeoModel = t.currentModel ?: return
            val textureIndex = if (identifier == null) t.textureIndex else 0
            val textureLocation = identifier ?: t.textureLocation
            val renderType = getRenderType(
                textureLocation,
                isBodyVisible(state) && !entity.isInvisibleTo(player),
                minecraft.shouldEntityAppearGlowing(entity),
                animatedGeoModel.getGeoModel().isTranslucentTexture(textureIndex)
            )
            val useExtraPlayer = t.isRenderLayersFirst
            val color = getRenderColor(t, partialTick, poseStack, multiBufferSource, null, packedLight)
            renderWithBone(
                animatedGeoModel,
                t,
                partialTick,
                poseStack,
                multiBufferSource,
                null,
                packedLight,
                packOverlayCoords(entity, getHurtOverlayProgress(entity, partialTick)),
                color.getRed() / 255.0f,
                color.getGreen() / 255.0f,
                color.getBlue() / 255.0f,
                color.getAlpha() / 255.0f
            )
            if (useExtraPlayer && !entity.isSpectator) {
                render(t, state, partialTick, poseStack, multiBufferSource, packedLight, event, modelData)
            }
            if (renderType != null) {
                renderWithBoneAndRenderType(
                    animatedGeoModel,
                    t,
                    partialTick,
                    renderType,
                    poseStack,
                    multiBufferSource,
                    textureIndex,
                    null,
                    packedLight,
                    packOverlayCoords(entity, getHurtOverlayProgress(entity, partialTick)),
                    color.getRed() / 255.0f,
                    color.getGreen() / 255.0f,
                    color.getBlue() / 255.0f,
                    color.getAlpha() / 255.0f
                )
            }
            if (!useExtraPlayer && !entity.isSpectator) {
                render(t, state, partialTick, poseStack, multiBufferSource, packedLight, event, modelData)
            }
            poseStack.popPose()
        }

        val activeCollector = RenderContext.collector()
        val activeCameraState = RenderContext.camera()
        if (activeCollector != null && activeCameraState != null && entity != minecraft.cameraEntity) {
            (this as LivingEntityRendererAccessor).`tlm$renderNameTag`(
                state,
                poseStack,
                activeCollector,
                activeCameraState
            )
        }
        RenderLivingBridge.firePost(entity, this, partialTick, poseStack, multiBufferSource, packedLight)
    }

    open fun render(
        entity: T,
        state: S,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLightIn: Int,
        event: AnimationEvent<*>,
        data: EntityModelData
    ) {
        for (layerRenderer in layerRenderers) {
            layerRenderer.render(
                state,
                poseStack,
                bufferSource,
                packedLightIn,
                entity,
                event.getLimbSwing(),
                event.getLimbSwingAmount(),
                partialTick,
                data.lerpedAge,
                data.rawNetHeadYaw,
                data.rawHeadPitch
            )
        }
    }

    open fun getHurtOverlayProgress(entity: TEntity, partialTick: Float): Float = 0.0f

    open fun preRenderCallback(entity: TEntity, poseStack: PoseStack, partialTick: Float) {
    }

    open fun setupRotations(
        entity: TEntity,
        state: S,
        poseStack: PoseStack,
        ageInTicks: Float,
        rotationYaw: Float,
        partialTicks: Float,
        scale: Float
    ) {
        var yaw = rotationYaw
        val t = entity.deathTime
        val zIsAutoSpinAttack = entity.isAutoSpinAttack
        if (t > 0) entity.deathTime = 0
        if (zIsAutoSpinAttack) entity.setLivingEntityFlag(4, false)
        if (entity.onClimbable()) {
            val lastClimbablePos = entity.lastClimbablePos
            if (lastClimbablePos.isPresent) {
                val optionalValue = entity.level().getBlockState(lastClimbablePos.get())
                    .getOptionalValue(HorizontalDirectionalBlock.FACING)
                if (optionalValue.isPresent) {
                    yaw = (optionalValue.get().opposite.get2DDataValue() * 90).toFloat()
                }
            }
        }
        super.setupRotations(state, poseStack, yaw, scale)
        if (t > 0) entity.deathTime = t
        if (zIsAutoSpinAttack) entity.setLivingEntityFlag(4, true)
    }

    override fun shouldShowName(entity: TEntity, distance: Double): Boolean {
        val d = if (entity.isDiscrete) 32.0 else 64.0
        return distance < d * d && entity == entityRenderDispatcher.crosshairPickEntity && entity.hasCustomName() && Minecraft.renderNames()
    }

    fun addLayerRenderer(layerRenderer: GeoLayerRenderer<T>): Boolean = layerRenderers.add(layerRenderer)

    override fun getCurrentRTB(): MultiBufferSource? = rtb

    override fun setCurrentRTB(bufferSource: MultiBufferSource?) {
        rtb = bufferSource
    }

    override fun extractRenderState(entity: TEntity, state: S, partialTick: Float) {
        super.extractRenderState(entity, state, partialTick)
        HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTick, itemModelResolver)
    }

    companion object {
        @JvmStatic
        fun packOverlayCoords(entity: LivingEntity, u: Float): Int {
            return OverlayTexture.pack(
                OverlayTexture.u(u),
                OverlayTexture.v(entity.hurtTime > 0 || entity.deathTime > 0)
            )
        }
    }
}