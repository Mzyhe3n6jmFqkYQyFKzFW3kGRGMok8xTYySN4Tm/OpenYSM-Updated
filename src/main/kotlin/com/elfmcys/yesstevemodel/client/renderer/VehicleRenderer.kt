package com.elfmcys.yesstevemodel.client.renderer

import com.elfmcys.yesstevemodel.capability.VehicleCapability
import com.elfmcys.yesstevemodel.client.entity.GeckoVehicleEntity
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoEntityRenderer
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity

open class VehicleRenderer(context: EntityRendererProvider.Context) :
    GeoEntityRenderer<Entity, GeckoVehicleEntity>(context) {

    open fun render(
        entity: Entity,
        state: EntityRenderState,
        entityYaw: Float,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int
    ) {
        val player = Minecraft.getInstance().player
        if (player == null || entity.isInvisibleTo(player)) {
            return
        }
        val cap = VehicleCapability[entity] ?: return
        cap.tickModel()
        renderEntity(cap, state, entityYaw, partialTick, poseStack, bufferSource, packedLight)
    }

    open fun getTextureLocation(entity: Entity): Identifier {
        return VehicleCapability[entity]?.getTextureLocation() ?: MissingTextureAtlasSprite.getLocation()
    }
}