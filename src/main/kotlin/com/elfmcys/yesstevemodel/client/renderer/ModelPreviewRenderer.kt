package com.elfmcys.yesstevemodel.client.renderer

import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.capability.VehicleCapability
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable
import com.elfmcys.yesstevemodel.client.entity.PlayerPreviewEntity
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoReplacedEntityRenderer
import com.elfmcys.yesstevemodel.geckolib3.util.RenderUtils
import com.elfmcys.yesstevemodel.util.AnimatableCacheUtil
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderDispatcher
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.util.Mth
import net.minecraft.world.entity.*
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Blocks
import org.joml.Quaternionf
import org.joml.Vector3f
import rip.ysm.compat.firstperson.FirstPersonCompat
import rip.ysm.compat.oculus.OculusCompat
import rip.ysm.compat.touhoulittlemaid.TouhouLittleMaidCompat

object ModelPreviewRenderer {
    @JvmField
    var isPreviewMode: Boolean = false

    @JvmField
    var isExtraPlayerMode: Boolean = false

    @JvmField
    var isFirstPersonMode: Boolean = false

    @JvmStatic
    fun setPreviewMode(previewMode: Boolean) {
        isPreviewMode = previewMode
    }

    @JvmStatic
    fun isPreview(): Boolean {
        return isPreviewMode
    }

    @JvmStatic
    fun setExtraPlayerMode(extraPlayerMode: Boolean) {
        isExtraPlayerMode = extraPlayerMode
    }

    @JvmStatic
    fun isExtraPlayer(): Boolean {
        return isExtraPlayerMode
    }

    @JvmStatic
    fun setFirstPersonMode(firstPersonMode: Boolean) {
        isFirstPersonMode = firstPersonMode
    }

    @JvmStatic
    fun isFirstPerson(): Boolean {
        return isFirstPersonMode || OculusCompat.isModLoaded || FirstPersonCompat.isFirstPersonActive()
    }

    @JvmStatic
    fun isFirstPersonOnRenderThread(): Boolean {
        RenderSystem.assertOnRenderThread()
        return isFirstPersonMode && !FirstPersonCompat.isFirstPersonActive()
    }

    @JvmStatic
    fun renderVehicleModel(entity: Entity, poseStack: PoseStack, partialTick: Float) {
        val vehicle = entity.vehicle
        if (vehicle != null) {
            val cap = VehicleCapability[vehicle]
            if (cap != null) {
                if (!cap.isModelInitialized() || !cap.isModelReady()) return
                val index = vehicle.passengers.indexOf(entity)
                val model = cap.getCurrentModel()
                if (index < 0 || model == null || model.passengerGroupChains()
                        .isEmpty() || index >= model.passengerGroupChains().size
                ) return
                val list = model.passengerGroupChains()[index]
                val bodyRotation = CustomVehicleRenderer.getBodyRotation(
                    vehicle,
                    Mth.lerp(partialTick, vehicle.yRotO, vehicle.yRot),
                    partialTick
                )
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - bodyRotation))
                RenderUtils.prepMatrixForLocator(poseStack, list)
                poseStack.mulPose(Axis.YN.rotationDegrees(180.0f - bodyRotation))
                var myRidingOffset = -(vehicle.getPassengerRidingPosition(entity).y - vehicle.y)
                if ((entity is Player && PlayerCapability[entity] != null) || TouhouLittleMaidCompat.isMaidRideable(
                        entity
                    )
                ) {
                    myRidingOffset -= 0.5
                }
                poseStack.translate(0.0, myRidingOffset, 0.0)
            }
        }
    }

    // 动画测试界面的模型
    @JvmStatic
    fun <TEntity : Player, TAnimatable : LivingAnimatable<TEntity>, TRenderState : AvatarRenderState> renderEntityPreview(
        x: Float,
        y: Float,
        scale: Float,
        pitch: Float,
        yaw: Float,
        partialTick: Float,
        animatableEntity: TAnimatable,
        state: TRenderState,
        renderer: GeoReplacedEntityRenderer<TEntity, TAnimatable, TRenderState>,
        renderGround: Boolean
    ) {
        setPreviewMode(true)
        val livingEntity = animatableEntity.entity
        val modelViewStack = RenderSystem.getModelViewStack()
        modelViewStack.pushMatrix()
        modelViewStack.translate(x, y, 1250.0f)
        modelViewStack.scale(1.0f, 1.0f, -1.0f)
        val poseStack = PoseStack()
        poseStack.translate(0.0, 0.0, 1000.0)
        poseStack.scale(scale, scale, scale)
        poseStack.translate(0.0, 0.8, 0.0)
        val rotationZ = Axis.ZP.rotationDegrees(180.0f)
        val rotationX = Axis.XP.rotationDegrees(10.0f + pitch)
        rotationZ.mul(rotationX)
        poseStack.mulPose(rotationZ)
        val oldBodyRot = livingEntity.yBodyRot
        val oldBodyRotO = livingEntity.yBodyRotO
        val oldYRot = livingEntity.yRot
        val oldYRotO = livingEntity.yRotO
        val oldXRot = livingEntity.xRot
        val oldXRotO = livingEntity.xRotO
        val oldHeadRotO = livingEntity.yHeadRotO
        val oldHeadRot = livingEntity.yHeadRot
        val oldPose = livingEntity.pose
        livingEntity.yBodyRot = -yaw
        livingEntity.yBodyRotO = -yaw
        livingEntity.yRot = 180.0f
        livingEntity.yRotO = 180.0f
        livingEntity.xRot = 0.0f
        livingEntity.xRotO = 0.0f
        livingEntity.yHeadRot = -yaw
        livingEntity.yHeadRotO = -yaw
        val entityRenderDispatcher = Minecraft.getInstance().entityRenderDispatcher
        rotationX.conjugate()
        poseStack.mulPose(rotationX)
        val bufferSource = Minecraft.getInstance().renderBuffers().bufferSource()
        val animationTracker = (animatableEntity as IPreviewAnimatable).getAnimationStateMachine()
        if (animationTracker.isCurrentAnimation("sleep")) {
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw - 90.0f))
            poseStack.translate(0.5, 0.5625, 0.0)
            livingEntity.pose = Pose.SLEEPING
        }
        if (animationTracker.isCurrentAnimation("swim") || animationTracker.isCurrentAnimation("swim_stand")) {
            livingEntity.pose = Pose.SWIMMING
        }
        if (animationTracker.isCurrentAnimation("sneak") || animationTracker.isCurrentAnimation("sneaking")) {
            livingEntity.pose = Pose.CROUCHING
        }
        if (animationTracker.isCurrentAnimation("sit")) {
            poseStack.translate(0.0, -0.5, 0.0)
        }
        if (animationTracker.isCurrentAnimation("ride")) {
            poseStack.translate(0.0, 0.85, 0.0)
        }
        if (animationTracker.isCurrentAnimation("ride_pig")) {
            poseStack.translate(0.0, 0.3125, 0.0)
        }
        if (animationTracker.isCurrentAnimation("boat")) {
            poseStack.translate(0.0, -0.45, 0.0)
        }
        renderVehicleForAnimation(yaw, animatableEntity, partialTick, poseStack, entityRenderDispatcher, bufferSource)
        if (animationTracker.isCurrentAnimation("sleep")) {
            renderBedPreview(scale, pitch, yaw, bufferSource)
        }
        if (renderGround) {
            renderGroundPreview(scale, pitch, yaw, bufferSource)
        }
        renderer.renderEntity(
            animatableEntity,
            state,
            0.0f,
            partialTick,
            poseStack,
            bufferSource,
            15728880
        )
        livingEntity.yBodyRot = oldBodyRot
        livingEntity.yBodyRotO = oldBodyRotO
        livingEntity.yRot = oldYRot
        livingEntity.yRotO = oldYRotO
        livingEntity.xRot = oldXRot
        livingEntity.xRotO = oldXRotO
        livingEntity.yHeadRotO = oldHeadRotO
        livingEntity.yHeadRot = oldHeadRot
        livingEntity.pose = oldPose
        modelViewStack.popMatrix()
        setPreviewMode(false)
    }

    @JvmStatic
    fun renderBedPreview(scale: Float, pitch: Float, yaw: Float, bufferSource: MultiBufferSource) {
        val poseStack = PoseStack()
        poseStack.translate(0.0, 0.0, 1000.0)
        poseStack.scale(scale, scale, scale)
        poseStack.translate(0.0, 0.8, 0.0)
        val rotationZ = Axis.ZP.rotationDegrees(180.0f)
        rotationZ.mul(Axis.XP.rotationDegrees(10.0f + pitch))
        poseStack.mulPose(rotationZ)
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw + 180.0f))
        poseStack.translate(-0.5, 0.0, 0.5)
        Minecraft.getInstance().blockRenderer.renderSingleBlock(
            Blocks.RED_BED.defaultBlockState(),
            poseStack,
            bufferSource,
            15728880,
            OverlayTexture.NO_OVERLAY
        )
    }

    @JvmStatic
    fun renderGroundPreview(scale: Float, pitch: Float, yaw: Float, bufferSource: MultiBufferSource) {
        val poseStack = PoseStack()
        poseStack.translate(0.0, 0.0, 1000.0)
        poseStack.scale(scale, scale, scale)
        poseStack.translate(0.0, 0.8, 0.0)
        val rotationZ = Axis.ZP.rotationDegrees(180.0f)
        rotationZ.mul(Axis.XP.rotationDegrees(10.0f + pitch))
        poseStack.mulPose(rotationZ)
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw))
        poseStack.translate(-1.5, -1.0, -2.5)
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                poseStack.translate(0.0f, 0.0f, 1.0f)
                Minecraft.getInstance().blockRenderer.renderSingleBlock(
                    Blocks.GRASS_BLOCK.defaultBlockState(),
                    poseStack,
                    bufferSource,
                    15728880,
                    OverlayTexture.NO_OVERLAY
                )
            }
            poseStack.translate(1.0f, 0.0f, -3.0f)
        }
        poseStack.translate(-1.0f, 1.0f, 1.0f)
        Minecraft.getInstance().blockRenderer.renderSingleBlock(
            Blocks.SHORT_GRASS.defaultBlockState(),
            poseStack,
            bufferSource,
            15728880,
            OverlayTexture.NO_OVERLAY
        )
        poseStack.translate(0.0f, 0.0f, 1.0f)
        Minecraft.getInstance().blockRenderer.renderSingleBlock(
            Blocks.RED_TULIP.defaultBlockState(),
            poseStack,
            bufferSource,
            15728880,
            OverlayTexture.NO_OVERLAY
        )
    }

    @JvmStatic
    fun renderVehicleForAnimation(
        yaw: Float,
        animatableEntity: AnimatableEntity<*>,
        partialTick: Float,
        poseStack: PoseStack,
        entityRenderDispatcher: EntityRenderDispatcher,
        bufferSource: MultiBufferSource
    ) {
        val entity = animatableEntity.entity
        val animationTracker = (animatableEntity as IPreviewAnimatable).getAnimationStateMachine()
        if (animationTracker.isCurrentAnimation("ride")) {
            AnimatableCacheUtil.ENTITIES_CACHE.get(EntityType.getKey(EntityType.HORSE)) {
                EntityType.HORSE.create(entity.level(), EntitySpawnReason.LOAD)
            }.let {
                renderVehicleEntity(
                    yaw,
                    entity,
                    poseStack,
                    entityRenderDispatcher,
                    bufferSource,
                    it,
                    partialTick
                )
            }
        } else if (animationTracker.isCurrentAnimation("ride_pig")) {
            AnimatableCacheUtil.ENTITIES_CACHE.get(EntityType.getKey(EntityType.PIG)) {
                EntityType.PIG.create(entity.level(), EntitySpawnReason.LOAD)
            }.let {
                renderVehicleEntity(
                    yaw,
                    entity,
                    poseStack,
                    entityRenderDispatcher,
                    bufferSource,
                    it,
                    partialTick
                )
            }
        } else if (animationTracker.isCurrentAnimation("boat")) {
            AnimatableCacheUtil.ENTITIES_CACHE.get(EntityType.getKey(EntityType.OAK_BOAT)) {
                EntityType.OAK_BOAT.create(entity.level(), EntitySpawnReason.LOAD)
            }.let {
                renderVehicleEntity(
                    yaw,
                    entity,
                    poseStack,
                    entityRenderDispatcher,
                    bufferSource,
                    it,
                    partialTick
                )
            }
        }
    }

    @JvmStatic
    fun renderVehicleEntity(
        yaw: Float,
        riderEntity: Entity,
        poseStack: PoseStack,
        entityRenderDispatcher: EntityRenderDispatcher,
        bufferSource: MultiBufferSource,
        vehicleEntity: Entity,
        partialTick: Float
    ) {
        poseStack.pushPose()
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw))
        poseStack.popPose()
    }

    @JvmStatic
    fun <T : Player, TAnimatable : LivingAnimatable<T>, S : AvatarRenderState> renderLivingEntityPreview(
        x: Float,
        y: Float,
        scale: Float,
        partialTick: Float,
        animatable: TAnimatable,
        state: S,
        renderer: GeoReplacedEntityRenderer<T, TAnimatable, S>,
        disablePreviewRotation: Boolean,
        hideEquipment: Boolean
    ) {
        val savedEquipment: Array<ItemStack?>?
        setPreviewMode(true)
        val livingEntity = animatable.entity as LivingEntity
        val modelViewStack = RenderSystem.getModelViewStack()
        modelViewStack.pushMatrix()
        modelViewStack.translate(x, y, 1050.0f)
        modelViewStack.scale(1.0f, 1.0f, -1.0f)
        val poseStack = PoseStack()
        poseStack.translate(0.0, if (disablePreviewRotation) 5.5 else 0.0, 1000.0)
        poseStack.scale(scale, scale, scale)
        val rotationZ = Axis.ZP.rotationDegrees(180.0f)
        val rotationX = Axis.XP.rotationDegrees(if (disablePreviewRotation) 0.0f else -10.0f)
        rotationZ.mul(rotationX)
        poseStack.mulPose(rotationZ)
        val oldBodyRot = livingEntity.yBodyRot
        val oldBodyRotO = livingEntity.yBodyRotO
        val oldYRot = livingEntity.yRot
        val oldYRotO = livingEntity.yRotO
        val oldXRot = livingEntity.xRot
        val oldXRotO = livingEntity.xRotO
        val oldHeadRotO = livingEntity.yHeadRotO
        val oldHeadRot = livingEntity.yHeadRot
        if (hideEquipment) {
            val slots = EquipmentSlot.entries
            savedEquipment = arrayOfNulls(slots.size)
            for ((slotIndex, equipmentSlot) in slots.withIndex()) {
                savedEquipment[slotIndex] = livingEntity.getItemBySlot(equipmentSlot).copy()
                livingEntity.setItemSlot(equipmentSlot, ItemStack.EMPTY)
            }
        } else {
            savedEquipment = null
        }
        val previewYaw = if (disablePreviewRotation) 180.0f else 200.0f
        livingEntity.yBodyRot = previewYaw
        livingEntity.yBodyRotO = previewYaw
        livingEntity.yRot = previewYaw
        livingEntity.yRotO = previewYaw
        livingEntity.xRot = 0.0f
        livingEntity.xRotO = 0.0f
        livingEntity.yHeadRot = livingEntity.yRot
        livingEntity.yHeadRotO = livingEntity.yRot
        val vehicle = livingEntity.vehicle
        if (vehicle is LivingEntity) {
            val vehicleYaw = vehicle.yRot
            poseStack.mulPose(Axis.YP.rotationDegrees(vehicleYaw - previewYaw))
            livingEntity.yHeadRot = vehicleYaw
            livingEntity.yHeadRotO = vehicleYaw
        }
        rotationX.conjugate()
        poseStack.mulPose(rotationX)
        val bufferSource = Minecraft.getInstance().renderBuffers().bufferSource()
        renderer.renderEntity(animatable, state, 0.0f, partialTick, poseStack, bufferSource, 15728880)
        livingEntity.yBodyRot = oldBodyRot
        livingEntity.yBodyRotO = oldBodyRotO
        livingEntity.yRot = oldYRot
        livingEntity.yRotO = oldYRotO
        livingEntity.xRot = oldXRot
        livingEntity.xRotO = oldXRotO
        livingEntity.yHeadRotO = oldHeadRotO
        livingEntity.yHeadRot = oldHeadRot
        if (savedEquipment != null) {
            for ((slotIndex, equipmentSlot) in EquipmentSlot.entries.withIndex()) {
                val itemStack = savedEquipment[slotIndex]
                if (itemStack != null) {
                    livingEntity.setItemSlot(equipmentSlot, itemStack)
                }
            }
        }
        modelViewStack.popMatrix()
        setPreviewMode(false)
    }

    @JvmStatic
    fun renderPlayerOverlay(
        guiGraphics: GuiGraphics,
        localPlayer: LocalPlayer,
        x: Double,
        y: Double,
        scale: Float,
        yawOffset: Float,
        zDepth: Int,
        partialTick: Float
    ) {
        setExtraPlayerMode(true)
        val modelViewStack = RenderSystem.getModelViewStack()
        modelViewStack.pushMatrix()
        modelViewStack.translate((x + scale * 0.5f).toFloat(), (y + scale * 2.0f).toFloat(), 0.0f)
        modelViewStack.scale(1.0f, 1.0f, -1.0f)
        val poseStack = PoseStack()
        poseStack.translate(0.0f, 0.0f, -zDepth.toFloat())
        poseStack.scale(scale, scale, scale)
        val rotationZ = Axis.ZP.rotationDegrees(180.1f)
        val rotationY = Axis.YP.rotationDegrees(
            Mth.lerp(
                partialTick,
                localPlayer.yBodyRotO,
                localPlayer.yBodyRot
            ) + yawOffset - 180.0f
        )
        rotationZ.mul(rotationY)
        poseStack.mulPose(rotationZ)
        rotationY.conjugate()
        poseStack.mulPose(rotationY)
        modelViewStack.popMatrix()
        setExtraPlayerMode(false)
    }

    @JvmStatic
    fun submitLivingEntityPreview(
        guiGraphics: GuiGraphics,
        x0: Int,
        y0: Int,
        x1: Int,
        y1: Int,
        displaySize: Int,
        partialTick: Float,
        animatable: PlayerPreviewEntity,
        disablePreviewRotation: Boolean,
        hideEquipment: Boolean
    ) {
        val entity = animatable.entity as LivingEntity
        val savedEquipment: Array<ItemStack?>?
        if (hideEquipment) {
            val slots = EquipmentSlot.entries
            savedEquipment = arrayOfNulls(slots.size)
            for ((i, slot) in slots.withIndex()) {
                savedEquipment[i] = entity.getItemBySlot(slot).copy()
                entity.setItemSlot(slot, ItemStack.EMPTY)
            }
        } else {
            savedEquipment = null
        }
        val renderer = RendererManager.getPlayerRenderer()
        val state = AvatarRenderState()
        renderer.extractRenderState(entity as Player, state, partialTick)
        state.lightCoords = LightTexture.FULL_BRIGHT
        val previewYaw = if (disablePreviewRotation) 180.0f else 200.0f
        state.bodyRot = previewYaw
        state.yRot = 0.0f
        state.xRot = 0.0f
        PreviewEntityRegistry.register(state, animatable)
        val rotation = Quaternionf().rotateZ(Math.PI.toFloat())
        var cameraTilt: Quaternionf? = null
        if (!disablePreviewRotation) {
            cameraTilt = Quaternionf().rotateX((-10.0 * Math.PI / 180.0).toFloat())
            rotation.mul(cameraTilt)
        }
        val entityScale = entity.scale
        val yOffsetPx = if (disablePreviewRotation) 5.5f else 0.0f
        val yOffsetModel = yOffsetPx * entityScale / displaySize.toFloat()
        val translation = Vector3f(0.0f, entity.bbHeight / 2.0f + yOffsetModel, 0.0f)
        val submitScale = displaySize.toFloat() / entityScale
        guiGraphics.enableScissor(x0, y0, x1, y1)
        guiGraphics.submitEntityRenderState(state, submitScale, translation, rotation, cameraTilt, x0, y0, x1, y1)
        guiGraphics.disableScissor()
        if (savedEquipment != null && entity is Player) {
            for ((i, slot) in EquipmentSlot.entries.withIndex()) {
                val item = savedEquipment[i]
                if (item != null) {
                    entity.setItemSlot(slot, item)
                }
            }
        }
    }

    @JvmStatic
    fun submitPlayerOverlay(
        guiGraphics: GuiGraphics,
        localPlayer: LocalPlayer,
        x: Double,
        y: Double,
        scale: Float,
        yawOffset: Float,
        partialTick: Float
    ) {
        val cap = PlayerCapability[localPlayer] ?: return
        cap.tickModel()
        val cx = (x + scale * 0.6f).toFloat()
        val cy = (y + scale * 1.0f).toFloat()
        val halfW = (scale * 2.0f).toInt()
        val halfH = (scale * 2.5f).toInt()
        val x0 = cx.toInt() - halfW
        val x1 = cx.toInt() + halfW
        val y0 = cy.toInt() - halfH
        val y1 = cy.toInt() + halfH
        val renderer = RendererManager.getPlayerRenderer()
        val state = AvatarRenderState()
        renderer.extractRenderState(localPlayer, state, partialTick)
        state.lightCoords = LightTexture.FULL_BRIGHT
        PreviewEntityRegistry.register(state, cap)
        state.bodyRot = 180.0f
        val rotation = Quaternionf().rotateZ(Math.PI.toFloat())
        val entityScale = localPlayer.scale
        val translation = Vector3f(0.0f, localPlayer.bbHeight / 2.0f, 0.0f)
        val submitScale = scale / entityScale
        guiGraphics.submitEntityRenderState(state, submitScale, translation, rotation, null, x0, y0, x1, y1)
    }

    @JvmStatic
    fun submitTexturePreview(
        guiGraphics: GuiGraphics,
        x0: Int,
        y0: Int,
        x1: Int,
        y1: Int,
        anchorX: Float,
        anchorY: Float,
        zoom: Float,
        pitch: Float,
        yaw: Float,
        animatable: PlayerPreviewEntity,
        renderGround: Boolean,
        partialTick: Float
    ) {
        val entity = animatable.entity as LivingEntity
        val tracker = animatable.getAnimationStateMachine()
        val oldPose = entity.pose
        var newPose = oldPose
        var poseYOffset = 0.0f
        when {
            tracker.isCurrentAnimation("sleep") -> {
                newPose = Pose.SLEEPING
                poseYOffset = 0.5625f
            }

            tracker.isCurrentAnimation("swim") || tracker.isCurrentAnimation("swim_stand") -> {
                newPose = Pose.SWIMMING
            }

            tracker.isCurrentAnimation("sneak") || tracker.isCurrentAnimation("sneaking") -> {
                newPose = Pose.CROUCHING
            }

            tracker.isCurrentAnimation("sit") -> {
                poseYOffset = -0.5f
            }

            tracker.isCurrentAnimation("ride") -> {
                poseYOffset = 0.85f
            }

            tracker.isCurrentAnimation("ride_pig") -> {
                poseYOffset = 0.3125f
            }

            tracker.isCurrentAnimation("boat") -> {
                poseYOffset = -0.45f
            }
        }
        val poseChanged = newPose != oldPose
        if (poseChanged) {
            entity.pose = newPose
        }
        val renderer = RendererManager.getPlayerRenderer()
        val state = AvatarRenderState()
        renderer.extractRenderState(entity as Player, state, partialTick)
        state.lightCoords = LightTexture.FULL_BRIGHT
        state.bodyRot = -yaw
        state.yRot = Mth.wrapDegrees(180.0f + yaw)
        state.xRot = 0.0f
        val wantBed = tracker.isCurrentAnimation("sleep")
        val wantHorse = tracker.isCurrentAnimation("ride")
        val wantPig = tracker.isCurrentAnimation("ride_pig")
        val wantBoat = tracker.isCurrentAnimation("boat")
        var scenery: PreviewEntityRegistry.SceneryRenderer? = null
        if (renderGround || wantBed || wantHorse || wantPig || wantBoat) {
            scenery = PreviewEntityRegistry.SceneryRenderer { poseStack, bufferSource, packedLight ->
                if (wantHorse) {
                    renderVehicleScenery(
                        poseStack,
                        bufferSource,
                        packedLight,
                        yaw,
                        partialTick,
                        entity,
                        EntityType.HORSE
                    )
                } else if (wantPig) {
                    renderVehicleScenery(
                        poseStack,
                        bufferSource,
                        packedLight,
                        yaw,
                        partialTick,
                        entity,
                        EntityType.PIG
                    )
                } else if (wantBoat) {
                    renderVehicleScenery(
                        poseStack,
                        bufferSource,
                        packedLight,
                        yaw,
                        partialTick,
                        entity,
                        EntityType.OAK_BOAT
                    )
                }
                if (wantBed) {
                    renderBedScenery(poseStack, bufferSource, packedLight, yaw)
                }
                if (renderGround) {
                    renderGroundScenery(poseStack, bufferSource, packedLight, yaw)
                }
            }
        }
        if (scenery != null || poseYOffset != 0.0f) {
            PreviewEntityRegistry.register(state, animatable, scenery, null, poseYOffset)
        } else {
            PreviewEntityRegistry.register(state, animatable)
        }
        val cameraTilt = Quaternionf().rotateX(Math.toRadians((-10.0 + pitch)).toFloat())
        val rotation = Quaternionf().rotateZ(Math.PI.toFloat()).mul(cameraTilt)
        val entityScale = entity.scale
        val submitScale = zoom / entityScale
        val rectCenterX = (x0 + x1) / 2.0f
        val rectCenterY = (y0 + y1) / 2.0f
        val translationX = (anchorX - rectCenterX) / submitScale
        val translationY = (anchorY - rectCenterY) / submitScale + 0.8f
        if (wantBed) {
            state.bodyRot = yaw - 90
        }
        val translation = Vector3f(translationX, translationY, 0.0f)
        guiGraphics.enableScissor(x0, y0, x1, y1)
        guiGraphics.submitEntityRenderState(state, submitScale, translation, rotation, cameraTilt, x0, y0, x1, y1)
        guiGraphics.disableScissor()
        if (poseChanged) {
            entity.pose = oldPose
        }
    }

    @JvmStatic
    private fun renderGroundScenery(
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int,
        yaw: Float
    ) {
        val blockRenderer = Minecraft.getInstance().blockRenderer
        poseStack.pushPose()
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw))
        poseStack.translate(-1.5, -1.0, -2.5)
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                poseStack.translate(0.0f, 0.0f, 1.0f)
                blockRenderer.renderSingleBlock(
                    Blocks.GRASS_BLOCK.defaultBlockState(),
                    poseStack,
                    bufferSource,
                    packedLight,
                    OverlayTexture.NO_OVERLAY
                )
            }
            poseStack.translate(1.0f, 0.0f, -3.0f)
        }
        poseStack.translate(-1.0f, 1.0f, 1.0f)
        blockRenderer.renderSingleBlock(
            Blocks.SHORT_GRASS.defaultBlockState(),
            poseStack,
            bufferSource,
            packedLight,
            OverlayTexture.NO_OVERLAY
        )
        poseStack.translate(0.0f, 0.0f, 1.0f)
        blockRenderer.renderSingleBlock(
            Blocks.RED_TULIP.defaultBlockState(),
            poseStack,
            bufferSource,
            packedLight,
            OverlayTexture.NO_OVERLAY
        )
        poseStack.popPose()
    }

    @JvmStatic
    private fun renderBedScenery(poseStack: PoseStack, bufferSource: MultiBufferSource, packedLight: Int, yaw: Float) {
        val collector = RenderContext.collector() ?: return
        poseStack.pushPose()
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw + 180.0f))
        poseStack.translate(-0.5, 0.0, 0.5)
        Minecraft.getInstance().modelManager.specialBlockModelRenderer().renderByBlock(
            Blocks.RED_BED,
            ItemDisplayContext.NONE,
            poseStack,
            collector,
            packedLight,
            OverlayTexture.NO_OVERLAY,
            -1
        )
        poseStack.popPose()
    }

    @JvmStatic
    private fun cachedVehicle(rider: LivingEntity, vehicleType: EntityType<out Entity>): Entity? {
        return runCatching {
            AnimatableCacheUtil.ENTITIES_CACHE.get(EntityType.getKey(vehicleType)) {
                vehicleType.create(rider.level(), EntitySpawnReason.LOAD)
            }
        }.getOrNull()
    }

    @JvmStatic
    private fun renderVehicleScenery(
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int,
        yaw: Float,
        partialTick: Float,
        rider: LivingEntity,
        vehicleType: EntityType<out Entity>
    ) {
        val vehicle = cachedVehicle(rider, vehicleType) ?: return
        val collector = RenderContext.collector()
        val cameraState = RenderContext.camera()
        if (collector == null || cameraState == null) {
            return
        }
        val dispatcher = Minecraft.getInstance().entityRenderDispatcher
        poseStack.pushPose()
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw))
        val yOffset = 0.0
        val vehicleState = dispatcher.extractEntity(vehicle, partialTick)
        vehicleState.lightCoords = packedLight
        vehicleState.shadowRadius = 0.0f
        vehicleState.shadowPieces.clear()
        dispatcher.submit(vehicleState, cameraState, 0.0, yOffset, 0.0, poseStack, collector)
        poseStack.popPose()
    }
}