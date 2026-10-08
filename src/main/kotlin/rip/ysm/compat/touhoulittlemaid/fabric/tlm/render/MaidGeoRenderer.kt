package rip.ysm.compat.touhoulittlemaid.fabric.tlm.render

import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer
import com.elfmcys.yesstevemodel.client.renderer.RenderContext
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.geo.IGeoRenderer
import com.elfmcys.yesstevemodel.geckolib3.util.EModelRenderCycle
import com.elfmcys.yesstevemodel.geckolib3.util.IRenderCycle
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.gecko.YsmMaidLayerBridge
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.state.EntityMaidRenderState
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.GeoLayerRenderer
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.IGeoEntity
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.IGeoEntityRenderer
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.math.Axis
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.Direction
import net.minecraft.util.Mth
import net.minecraft.world.entity.Pose
import org.joml.Matrix4f
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidAnimatable
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidRenderStore
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.anim.MaidPoseOffset
import kotlin.math.cos

@Environment(EnvType.CLIENT)
open class MaidGeoRenderer : IGeoRenderer<MaidAnimatable>, IGeoEntityRenderer<EntityMaidRenderState> {
    private val tlmLayerRenderers: MutableList<GeoLayerRenderer<*, *>> = ArrayList()
    private val dispatchedMat: Matrix4f = Matrix4f()
    private val renderEarlyMat: Matrix4f = Matrix4f()
    private var rtb: MultiBufferSource? = null
    override var currentModelRenderCycle: IRenderCycle = EModelRenderCycle.INITIAL

    override fun getGeoEntity(state: EntityMaidRenderState): IGeoEntity? {
        val maid = state.maid ?: return null
        return MaidRenderStore.getOrCreate(maid)
    }

    override fun addGeoLayerRenderer(layerRenderer: GeoLayerRenderer<*, *>) {
        tlmLayerRenderers.add(layerRenderer)
    }

    override fun geoRender(
        state: EntityMaidRenderState,
        entityYaw: Float,
        partialTick: Float,
        poseStack: PoseStack,
        collector: SubmitNodeCollector,
        packedLight: Int
    ) {
        val maid = state.maid ?: return
        val animatable = MaidRenderStore.getOrCreate(maid)

        val bufferSource = Minecraft.getInstance().renderBuffers().bufferSource()
        RenderContext.enter(collector, state.camera)
        try {
            currentRTB = bufferSource
            renderMaid(animatable, state, entityYaw, partialTick, poseStack, bufferSource, collector, packedLight)
            bufferSource.endBatch()
        } finally {
            RenderContext.exit()
        }
    }

    private fun renderMaid(
        animatable: MaidAnimatable,
        state: EntityMaidRenderState,
        entityYaw: Float,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        collector: SubmitNodeCollector,
        packedLight: Int
    ) {
        val maid = animatable.entity
        val vanillaState: LivingEntityRenderState = state
        val syncRotationsForPreview = ModelPreviewRenderer.isPreview
        var savedYBodyRot = 0.0f
        var savedYBodyRotO = 0.0f
        var savedYHeadRot = 0.0f
        var savedYHeadRotO = 0.0f
        var savedYRot = 0.0f
        var savedYRotO = 0.0f
        var savedXRot = 0.0f
        var savedXRotO = 0.0f
        if (syncRotationsForPreview) {
            savedYBodyRot = maid.yBodyRot
            savedYBodyRotO = maid.yBodyRotO
            savedYHeadRot = maid.yHeadRot
            savedYHeadRotO = maid.yHeadRotO
            savedYRot = maid.yRot
            savedYRotO = maid.yRotO
            savedXRot = maid.xRot
            savedXRotO = maid.xRotO

            val bodyRot = vanillaState.bodyRot
            val headYaw = vanillaState.bodyRot + vanillaState.yRot
            maid.yBodyRot = bodyRot
            maid.yBodyRotO = bodyRot
            maid.yHeadRot = headYaw
            maid.yHeadRotO = headYaw
            maid.yRot = headYaw
            maid.yRotO = headYaw
            maid.xRot = vanillaState.xRot
            maid.xRotO = vanillaState.xRot
        }

        val event: AnimationEvent<*>?
        try {
            event = animatable.processAnimation(partialTick)
        } finally {
            if (syncRotationsForPreview) {
                maid.yBodyRot = savedYBodyRot
                maid.yBodyRotO = savedYBodyRotO
                maid.yHeadRot = savedYHeadRot
                maid.yHeadRotO = savedYHeadRotO
                maid.yRot = savedYRot
                maid.yRotO = savedYRotO
                maid.xRot = savedXRot
                maid.xRotO = savedXRotO
            }
        }

        val minecraft = Minecraft.getInstance()
        if (event == null || minecraft.player == null) {
            return
        }

        val modelData = event.modelData
        dispatchedMat.set(poseStack.last().pose())
        currentModelRenderCycle = EModelRenderCycle.INITIAL

        poseStack.pushPose()
        if (vanillaState.hasPose(Pose.SLEEPING)) {
            val bedOrientation = vanillaState.bedOrientation
            if (bedOrientation != null) {
                val eyeHeight = maid.getEyeHeight(Pose.STANDING) - 0.1f
                poseStack.translate(
                    (-bedOrientation.stepX).toFloat() * eyeHeight,
                    0.0f,
                    (-bedOrientation.stepZ).toFloat() * eyeHeight
                )
            }
        }

        if (!syncRotationsForPreview) {
            val poseYOffset = MaidPoseOffset.resolve(maid, animatable)
            if (poseYOffset != 0.0f) {
                poseStack.translate(0.0f, poseYOffset, 0.0f)
            }
        }

        setupRotations(state, poseStack, modelData.lerpBodyRot, 1.0f)
        preRenderCallback(poseStack)
        poseStack.translate(0.0f, 0.01f, 0.0f)

        val geoModel = animatable.currentModel ?: run {
            poseStack.popPose()
            return
        }
        val textureIndex = animatable.textureIndex
        val texture = animatable.textureLocation
        val bodyVisible = !vanillaState.isInvisible
        val renderType = getRenderType(
            texture,
            bodyVisible,
            minecraft.shouldEntityAppearGlowing(maid),
            geoModel.geoModel.isTranslucentTexture(textureIndex)
        )

        val layersFirst = animatable.isRenderLayersFirst
        val color = getRenderColor(animatable, partialTick, poseStack, bufferSource, null, packedLight)
        val overlay = packOverlayCoords(state)

        renderWithBone(
            geoModel,
            animatable,
            partialTick,
            poseStack,
            bufferSource,
            null,
            packedLight,
            overlay,
            color.red / 255.0f,
            color.green / 255.0f,
            color.blue / 255.0f,
            color.alpha / 255.0f
        )
        if (layersFirst) {
            renderTlmLayers(animatable, state, poseStack, collector)
        }
        if (renderType != null) {
            renderWithBoneAndRenderType(
                geoModel,
                animatable,
                partialTick,
                renderType,
                poseStack,
                bufferSource,
                textureIndex,
                null,
                packedLight,
                overlay,
                color.red / 255.0f,
                color.green / 255.0f,
                color.blue / 255.0f,
                color.alpha / 255.0f
            )
        }
        if (!layersFirst) {
            renderTlmLayers(animatable, state, poseStack, collector)
        }
        poseStack.popPose()
    }

    private fun renderTlmLayers(
        animatable: MaidAnimatable,
        state: EntityMaidRenderState,
        poseStack: PoseStack,
        collector: SubmitNodeCollector
    ) {
        YsmMaidLayerBridge.submitMaidLayers(
            tlmLayerRenderers,
            collector,
            poseStack,
            state,
            animatable.getGeoModel()
        )
    }

    private fun setupRotations(state: LivingEntityRenderState, poseStack: PoseStack, bodyRot: Float, scale: Float) {
        var rot = bodyRot
        if (state.isFullyFrozen)
            rot += (cos((Mth.floor(state.ageInTicks) * 3.25f).toDouble()) * Math.PI * 0.4f).toFloat()
        val sleeping = state.hasPose(Pose.SLEEPING)
        if (!sleeping) poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - rot))
        when {
            sleeping -> {
                val bedOrientation = state.bedOrientation
                val sleepRot = if (bedOrientation != null) sleepDirectionToRotation(bedOrientation) else rot
                poseStack.mulPose(Axis.YP.rotationDegrees(sleepRot))
                poseStack.mulPose(Axis.ZP.rotationDegrees(FLIP_DEGREES))
                poseStack.mulPose(Axis.YP.rotationDegrees(270.0f))
            }

            state.isUpsideDown -> {
                poseStack.translate(0.0f, (state.boundingBoxHeight + 0.1f) / scale, 0.0f)
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f))
            }
        }
    }

    private fun sleepDirectionToRotation(direction: Direction): Float {
        return when (direction) {
            Direction.SOUTH -> 90.0f
            Direction.WEST -> 0.0f
            Direction.NORTH -> 270.0f
            Direction.EAST -> 180.0f
            else -> 0.0f
        }
    }

    private fun packOverlayCoords(state: LivingEntityRenderState): Int {
        return OverlayTexture.pack(OverlayTexture.u(0.0f), OverlayTexture.v(state.hasRedOverlay))
    }

    private fun preRenderCallback(poseStack: PoseStack) {
    }

    override fun renderEarly(
        animatable: MaidAnimatable,
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

    override var currentRTB: MultiBufferSource?
        get() = rtb
        set(value) {
            rtb = value
        }

    companion object {
        private const val FLIP_DEGREES: Float = 90.0f
    }
}
