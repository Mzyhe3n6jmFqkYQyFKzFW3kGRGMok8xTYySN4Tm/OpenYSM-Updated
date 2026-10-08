package com.elfmcys.yesstevemodel.client.renderer

import com.elfmcys.yesstevemodel.capability.ProjectileCapability
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.Minecraft
import net.minecraft.client.Options
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderDispatcher
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.util.Mth
import net.minecraft.world.entity.HumanoidArm
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.FishingHook
import net.minecraft.world.phys.Vec3
import rip.ysm.api.item.ToolActionBridge
import rip.ysm.compat.oculus.OculusCompat

object CustomFishingHookRenderer {
    @JvmStatic
    fun tryRenderCustomHook(
        fishingHook: FishingHook,
        state: EntityRenderState,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int
    ): Boolean {
        val cap = ProjectileCapability[fishingHook]
        if (cap != null && cap.isModelInitialized && cap.isModelReady) {
            fishingHook.xRot = 0.0f
            fishingHook.xRotO = 0.0f
            RendererManager.projectileRenderer
                .render(cap.entity, state, partialTick, poseStack, bufferSource, packedLight)
            val playerOwner: Player? = fishingHook.playerOwner
            if (playerOwner != null) {
                poseStack.pushPose()
                renderFishingLine(fishingHook, partialTick, poseStack, bufferSource, playerOwner)
                poseStack.popPose()
            }
            return false
        }
        return true
    }

    @JvmStatic
    fun renderFishingLine(
        fishingHook: FishingHook,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        player: Player
    ) {
        var hand = if (player.mainArm == HumanoidArm.RIGHT) 1 else -1
        if (!ToolActionBridge.canFishingRodCast(player.mainHandItem)) hand = -hand
        val swingProgressSqrt = Mth.sin(Mth.sqrt(player.getAttackAnim(partialTick)).toDouble() * Math.PI)
        val yawOffset = Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot) * 0.017453292f
        val dSin = Mth.sin(yawOffset.toDouble())
        val dCos = Mth.cos(yawOffset.toDouble())
        val handOffset = hand * 0.35
        val anglerX: Double
        val anglerY: Double
        val anglerZ: Double
        val anglerEye: Float
        val entityRenderDispatcher: EntityRenderDispatcher = Minecraft.getInstance().entityRenderDispatcher
        val options: Options = entityRenderDispatcher.options
        val camera = entityRenderDispatcher.camera
        if (camera == null || !options.cameraType.isFirstPerson || player != Minecraft.getInstance().player) {
            anglerX = Mth.lerp(partialTick.toDouble(), player.xo, player.x) - dCos * handOffset - dSin * 0.8
            anglerY = player.yo + player.eyeHeight.toDouble() + (player.y - player.yo) * partialTick.toDouble() - 0.45
            anglerZ = Mth.lerp(partialTick.toDouble(), player.zo, player.z) - dSin * handOffset + dCos * 0.8
            anglerEye = if (player.isCrouching) -0.1875f else 0.0f
        } else {
            val vec3XRot: Vec3 = camera.nearPlane.getPointOnPlane(hand * 0.525f, -0.1f)
                .scale(960.0 / options.fov().get().toDouble())
                .yRot(swingProgressSqrt * 0.5f)
                .xRot(-swingProgressSqrt * 0.7f)
            anglerX = Mth.lerp(partialTick.toDouble(), player.xo, player.x) + vec3XRot.x
            anglerY = Mth.lerp(partialTick.toDouble(), player.yo, player.y) + vec3XRot.y
            anglerZ = Mth.lerp(partialTick.toDouble(), player.zo, player.z) + vec3XRot.z
            anglerEye = player.eyeHeight
        }
        val startX = (anglerX - Mth.lerp(partialTick.toDouble(), fishingHook.xo, fishingHook.x)).toFloat()
        val startY =
            (anglerY - (Mth.lerp(partialTick.toDouble(), fishingHook.yo, fishingHook.y) + 0.25)).toFloat() + anglerEye
        val startZ = (anglerZ - Mth.lerp(partialTick.toDouble(), fishingHook.zo, fishingHook.z)).toFloat()
        val color = lineColor(fishingHook)
        val buffer: VertexConsumer = bufferSource.getBuffer(RenderTypes.lines())
        val poseLast: PoseStack.Pose = poseStack.last()
        for (size in 0..16) {
            stringVertex(
                startX,
                startY,
                startZ,
                buffer,
                poseLast,
                fraction(size),
                fraction(size + 1),
                color[0],
                color[1],
                color[2]
            )
        }
        if (OculusCompat.isModLoaded)
            buffer.addVertex(0.0f, 0.0f, 0.0f).setColor(0, 0, 0, 255).setNormal(0.0f, 0.0f, 0.0f)
    }

    @JvmStatic
    fun lineColor(fishingHook: FishingHook): FloatArray = floatArrayOf(0.0f, 0.0f, 0.0f)

    @JvmStatic
    fun fraction(i: Int): Float = i / 16.0f

    @JvmStatic
    fun stringVertex(
        x: Float,
        y: Float,
        z: Float,
        vertexConsumer: VertexConsumer,
        pose: PoseStack.Pose,
        startFrac: Float,
        endFrac: Float,
        red: Float,
        green: Float,
        blue: Float
    ) {
        val vx = x * startFrac
        val vy = y * (startFrac * startFrac + startFrac) * 0.5f + 0.25f
        val vz = z * startFrac
        val dx = x * endFrac - vx
        val dy = y * (endFrac * endFrac + endFrac) * 0.5f + 0.25f - vy
        val dz = z * endFrac - vz
        val length = Mth.sqrt(dx * dx + dy * dy + dz * dz)
        vertexConsumer.addVertex(pose, vx, vy, vz).setColor(red, green, blue, 1.0f)
            .setNormal(pose, dx / length, dy / length, dz / length)
    }
}