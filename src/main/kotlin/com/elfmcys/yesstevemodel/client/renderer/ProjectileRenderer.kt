package com.elfmcys.yesstevemodel.client.renderer

import com.elfmcys.yesstevemodel.capability.ProjectileCapability
import com.elfmcys.yesstevemodel.client.entity.GeckoProjectileEntity
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.projectile.Projectile

open class ProjectileRenderer(context: EntityRendererProvider.Context) :
    AbstractProjectileRenderer<Projectile, GeckoProjectileEntity, EntityRenderState>(context) {

    override fun createRenderState(): EntityRenderState = EntityRenderState()

    open fun render(
        projectile: Projectile,
        state: EntityRenderState,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int
    ) {
        val player = Minecraft.getInstance().player
        if (player == null || projectile.isInvisibleTo(player)) {
            return
        }
        val cap = ProjectileCapability[projectile] ?: return
        cap.tickModel()
        render(cap, state, partialTick, poseStack, bufferSource, packedLight)
    }

    open fun getTextureLocation(projectile: Projectile): Identifier {
        return ProjectileCapability[projectile]?.getTextureLocation() ?: MissingTextureAtlasSprite.getLocation()
    }
}