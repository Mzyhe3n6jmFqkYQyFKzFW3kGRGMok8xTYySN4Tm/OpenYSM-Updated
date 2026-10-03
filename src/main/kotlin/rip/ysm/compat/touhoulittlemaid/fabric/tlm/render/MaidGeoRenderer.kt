package rip.ysm.compat.touhoulittlemaid.fabric.tlm.render

import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer
import com.elfmcys.yesstevemodel.client.renderer.RenderContext
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.core.util.Color
import com.elfmcys.yesstevemodel.geckolib3.geo.IGeoRenderer
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel
import com.elfmcys.yesstevemodel.geckolib3.model.provider.data.EntityModelData
import com.elfmcys.yesstevemodel.geckolib3.util.EModelRenderCycle
import com.elfmcys.yesstevemodel.geckolib3.util.IRenderCycle
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.gecko.YsmMaidLayerBridge
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.state.EntityMaidRenderState
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.GeoLayerRenderer
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.IGeoEntity
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.IGeoEntityRenderer
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.math.Axis
import net.minecraft.world.entity.LivingEntity
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.Direction
import net.minecraft.resources.Identifier
import net.minecraft.util.Mth
import net.minecraft.world.entity.Pose
import org.jetbrains.annotations.Nullable
import org.joml.Matrix4f
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidAnimatable
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidRenderStore
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.anim.MaidPoseOffset
import java.util.List

open class MaidGeoRenderer : IGeoRenderer<MaidAnimatable>, IGeoEntityRenderer<EntityMaidRenderState> {
    val tlmLayerRenderers: MutableList<GeoLayerRenderer<*, *>> = java()
    var dispatchedMat: Matrix4f = Matrix4f()
    var renderEarlyMat: Matrix4f = Matrix4f()
    var rtb: MultiBufferSource = null
    var currentModelRenderCycle: IRenderCycle = EModelRenderCycle.INITIAL
    open fun getGeoEntity(state: EntityMaidRenderState): IGeoEntity {
        var maid: EntityMaid = state.maid
        return MaidRenderStore.getOrCreate(maid)
    }
    open fun addGeoLayerRenderer(layerRenderer: GeoLayerRenderer<*, *>) {
        this.tlmLayerRenderers.add(layerRenderer)
    }
    open fun geoRender(state: EntityMaidRenderState, entityYaw: Float, partialTick: Float, poseStack: PoseStack, collector: SubmitNodeCollector, packedLight: Int) {
        var maid: EntityMaid = state.maid
        if (maid == null) {
             }
        var animatable: MaidAnimatable = MaidRenderStore.getOrCreate(maid)
        var bufferSource: MultiBufferSource = Minecraft.getInstance().renderBuffers().bufferSource()
        RenderContext.enter(collector, state.camera)
        try {
            setCurrentRTB(bufferSource)
            renderMaid(animatable, state, entityYaw, partialTick, poseStack, bufferSource, collector, packedLight)
            bufferSource.endBatch()
        } finally {
            RenderContext.exit()
        }
    }
    open fun renderMaid(animatable: MaidAnimatable, state: EntityMaidRenderState, entityYaw: Float, partialTick: Float, poseStack: PoseStack, bufferSource: MultiBufferSource, collector: SubmitNodeCollector, packedLight: Int) {
        var maid: EntityMaid = animatable.getEntity()
        if (maid == null) {
             }
        var vanillaMaid: LivingEntity = maid
        var vanillaState: LivingEntityRenderState = state
        var syncRotationsForPreview: Boolean = ModelPreviewRenderer.isPreview()
        var savedYBodyRot: Float = 0.0f
        var savedYBodyRotO: Float = 0.0f
        var savedYHeadRot: Float = 0.0f
        var savedYHeadRotO: Float = 0.0f
        var savedYRot: Float = 0.0f
        var savedYRotO: Float = 0.0f
        var savedXRot: Float = 0.0f
        var savedXRotO: Float = 0.0f
        if (syncRotationsForPreview) {
            savedYBodyRot = vanillaMaid.yBodyRot
            savedYBodyRotO = vanillaMaid.yBodyRotO
            savedYHeadRot = vanillaMaid.yHeadRot
            savedYHeadRotO = vanillaMaid.yHeadRotO
            savedYRot = vanillaMaid.getYRot()
            savedYRotO = vanillaMaid.yRotO
            savedXRot = vanillaMaid.getXRot()
            savedXRotO = vanillaMaid.xRotO
            var bodyRot: Float = vanillaState.bodyRot
            var headYaw: Float = vanillaState.bodyRot + vanillaState.yRot
            vanillaMaid.yBodyRot = bodyRot
            vanillaMaid.yBodyRotO = bodyRot
            vanillaMaid.yHeadRot = headYaw
            vanillaMaid.yHeadRotO = headYaw
            vanillaMaid.setYRot(headYaw)
            vanillaMaid.yRotO = headYaw
            vanillaMaid.setXRot(vanillaState.xRot)
            vanillaMaid.xRotO = vanillaState.xRot
        }
        var event: AnimationEvent<*>
        try {
            event = animatable.processAnimation(partialTick)
        } finally {
            if (syncRotationsForPreview) {
                vanillaMaid.yBodyRot = savedYBodyRot
                vanillaMaid.yBodyRotO = savedYBodyRotO
                vanillaMaid.yHeadRot = savedYHeadRot
                vanillaMaid.yHeadRotO = savedYHeadRotO
                vanillaMaid.setYRot(savedYRot)
                vanillaMaid.yRotO = savedYRotO
                vanillaMaid.setXRot(savedXRot)
                vanillaMaid.xRotO = savedXRotO
            }
        }
        var minecraft: Minecraft = Minecraft.getInstance()
        if (event == null || minecraft.player == null) {
             }
        var modelData: EntityModelData = event.getModelData()
        this.dispatchedMat.set(poseStack.last().pose())
        setCurrentModelRenderCycle(EModelRenderCycle.INITIAL)
        poseStack.pushPose()
        if (vanillaState.hasPose(Pose.SLEEPING)) {
            var bedOrientation: Direction = vanillaState.bedOrientation
            if (bedOrientation != null) {
                var eyeHeight: Float = vanillaMaid.getEyeHeight(Pose.STANDING) - 0.1f
                poseStack.translate(-bedOrientation.getStepX() * eyeHeight, 0.0f, -bedOrientation.getStepZ() * eyeHeight)
            }
        }
        if (!syncRotationsForPreview) {
            var poseYOffset: Float = MaidPoseOffset.resolve(maid, animatable)
            if (poseYOffset != 0.0f) {
                poseStack.translate(0.0f, poseYOffset, 0.0f)
            }
        }
        setupRotations(state, poseStack, modelData.lerpBodyRot, 1.0f)
        preRenderCallback(poseStack)
        poseStack.translate(0.0f, 0.01f, 0.0f)
        var geoModel: AnimatedGeoModel = animatable.getCurrentModel()
        var textureIndex: Int = animatable.getTextureIndex()
        var texture: Identifier = animatable.getTextureLocation()
        var bodyVisible: Boolean = !vanillaState.isInvisible
        var renderType: RenderType = getRenderType(texture, bodyVisible, minecraft.shouldEntityAppearGlowing(vanillaMaid), geoModel.getGeoModel().isTranslucentTexture(textureIndex))
        var layersFirst: Boolean = animatable.isRenderLayersFirst()
        var color: Color = getRenderColor(animatable, partialTick, poseStack, bufferSource, null, packedLight)
        var overlay: Int = packOverlayCoords(state)
        renderWithBone(geoModel, animatable, partialTick, poseStack, bufferSource, null, packedLight, overlay, color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f)
        if (layersFirst) {
            renderTlmLayers(animatable, state, poseStack, collector)
        }
        if (renderType != null) {
            renderWithBoneAndRenderType(geoModel, animatable, partialTick, renderType, poseStack, bufferSource, textureIndex, null, packedLight, overlay, color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f)
        }
        if (!layersFirst) {
            renderTlmLayers(animatable, state, poseStack, collector)
        }
        poseStack.popPose()
    }
    open fun renderTlmLayers(animatable: MaidAnimatable, state: EntityMaidRenderState, poseStack: PoseStack, collector: SubmitNodeCollector) {
        YsmMaidLayerBridge.submitMaidLayers(this.tlmLayerRenderers, collector, poseStack, state, animatable.getGeoModel())
    }
    open fun setupRotations(state: LivingEntityRenderState, poseStack: PoseStack, bodyRot: Float, scale: Float) {
        var rot: Float = bodyRot
        if (state.isFullyFrozen) {
            rot += (Math.cos(Mth.floor(state.ageInTicks) * 3.25f) * Math.PI * 0.4f as Float)
        }
        var sleeping: Boolean = state.hasPose(Pose.SLEEPING)
        if (!sleeping) {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - rot))
        }
        if (sleeping) {
            var bedOrientation: Direction = state.bedOrientation
            var sleepRot: Float = if (bedOrientation != null) sleepDirectionToRotation(bedOrientation) else rot
            poseStack.mulPose(Axis.YP.rotationDegrees(sleepRot))
            poseStack.mulPose(Axis.ZP.rotationDegrees(FLIP_DEGREES))
            poseStack.mulPose(Axis.YP.rotationDegrees(270.0f))
        } else {
            if (state.isUpsideDown) {
                poseStack.translate(0.0f, state.boundingBoxHeight + 0.1f / scale, 0.0f)
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f))
            }
        }
    }
    open fun preRenderCallback(poseStack: PoseStack)
    open fun renderEarly(animatable: MaidAnimatable, poseStack: PoseStack, partialTick: Float, bufferSource: MultiBufferSource, buffer: VertexConsumer, packedLight: Int, packedOverlay: Int, red: Float, green: Float, blue: Float, alpha: Float) {
        this.renderEarlyMat.set(poseStack.last().pose())
        IGeoRenderer
    }
    open fun getCurrentRTB(): MultiBufferSource {
        this.rtb
    }
    open fun setCurrentRTB(bufferSource: MultiBufferSource) {
        this.rtb = bufferSource
    }
    open fun getCurrentModelRenderCycle(): IRenderCycle {
        this.currentModelRenderCycle
    }
    open fun setCurrentModelRenderCycle(cycle: IRenderCycle) {
        this.currentModelRenderCycle = cycle
    }
    companion object {
        @JvmField var FLIP_DEGREES: Float = 90.0f
        @JvmStatic fun sleepDirectionToRotation(direction: Direction): Float {
        }
        @JvmStatic fun packOverlayCoords(state: LivingEntityRenderState): Int {
            OverlayTexture.pack(OverlayTexture.u(0.0f), OverlayTexture.v(state.hasRedOverlay))
        }
    }
}