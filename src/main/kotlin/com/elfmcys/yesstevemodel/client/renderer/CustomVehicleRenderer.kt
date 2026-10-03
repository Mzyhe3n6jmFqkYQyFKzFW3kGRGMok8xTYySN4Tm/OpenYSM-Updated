package com.elfmcys.yesstevemodel.client.renderer

import com.elfmcys.yesstevemodel.capability.VehicleCapability
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior
import net.minecraft.world.phys.Vec3
import rip.ysm.api.entity.EntityDataBridge
import kotlin.math.atan2

object CustomVehicleRenderer {
    /**
     * 该实体是否真有一套可渲染的 YSM 载具模型。
     */
    @JvmStatic
    fun hasReadyVehicleModel(entity: Entity): Boolean {
        val cap = VehicleCapability[entity]
        return cap != null && cap.isModelInitialized() && cap.isModelReady()
    }

    @JvmStatic
    fun renderVehicle(
        entity: Entity,
        state: EntityRenderState,
        yaw: Float,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int
    ): Boolean {
        val cap = VehicleCapability[entity]
        if (cap != null && cap.isModelInitialized() && cap.isModelReady()) {
            RendererManager.getVehicleRenderer().renderEntity(
                cap,
                state,
                getBodyRotation(entity, yaw, partialTick),
                partialTick,
                poseStack,
                bufferSource,
                packedLight
            )
            return false
        }
        return true
    }

    @JvmStatic
    fun getBodyRotation(entity: Entity, entityYaw: Float, partialTick: Float): Float {
        var bodyRotation = entityYaw
        if (entity is LivingEntity) {
            bodyRotation = getLivingBodyRotation(entity, partialTick)
        } else if (entity is AbstractMinecart) {
            bodyRotation = getMinecartBodyRotation(entity, partialTick, bodyRotation)
        }
        return bodyRotation
    }

    @JvmStatic
    private fun getLivingBodyRotation(entity: LivingEntity, partialTick: Float): Float {
        var bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot)
        val headYaw = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot)

        val vehicle = entity.vehicle
        if (entity.isPassenger && vehicle != null && EntityDataBridge.shouldRiderSit(vehicle)) {
            if (vehicle is LivingEntity) {
                val vehicleBodyYaw = Mth.rotLerp(partialTick, vehicle.yBodyRotO, vehicle.yBodyRot)
                val yawDiff = Mth.clamp(Mth.wrapDegrees(headYaw - vehicleBodyYaw), -85.0f, 85.0f)
                bodyYaw = headYaw - yawDiff

                if (yawDiff * yawDiff > 2500.0f) {
                    bodyYaw += yawDiff * 0.2f
                }
            }
        }
        return bodyYaw
    }

    @JvmStatic
    private fun getMinecartBodyRotation(minecart: AbstractMinecart, partialTick: Float, defaultYaw: Float): Float {
        val interpX = Mth.lerp(partialTick.toDouble(), minecart.xOld, minecart.x)
        val interpY = Mth.lerp(partialTick.toDouble(), minecart.yOld, minecart.y)
        val interpZ = Mth.lerp(partialTick.toDouble(), minecart.zOld, minecart.z)
        val behavior: MinecartBehavior = minecart.behavior
        val interpPos: Vec3? = if (behavior is OldMinecartBehavior) {
            behavior.getPos(interpX, interpY, interpZ)
        } else {
            Vec3(interpX, interpY, interpZ)
        }

        var calculatedYaw = defaultYaw

        if (interpPos != null) {
            val frontOffsetPos: Vec3 = (if (behavior is OldMinecartBehavior) {
                behavior.getPosOffs(interpX, interpY, interpZ, 0.30000001192092896)
            } else {
                Vec3(interpX, interpY, interpZ)
            }) ?: interpPos

            val backOffsetPos: Vec3 = (if (behavior is OldMinecartBehavior) {
                behavior.getPosOffs(interpX, interpY, interpZ, -0.30000001192092896)
            } else {
                Vec3(interpX, interpY, interpZ)
            }) ?: interpPos

            val directionVec: Vec3 = backOffsetPos.add(-frontOffsetPos.x, -frontOffsetPos.y, -frontOffsetPos.z)
            if (directionVec.length() != 0.0) {
                calculatedYaw = (atan2(directionVec.z, directionVec.x) * 180.0 / Math.PI).toFloat()
            }
        }
        return calculatedYaw
    }
}