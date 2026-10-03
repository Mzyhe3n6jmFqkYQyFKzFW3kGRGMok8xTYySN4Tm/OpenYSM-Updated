package com.elfmcys.yesstevemodel.client.renderer

import com.elfmcys.yesstevemodel.capability.ProjectileCapability
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.world.entity.projectile.Projectile

object CustomProjectileRenderer {
    @JvmStatic
    fun renderProjectile(
        projectile: Projectile,
        state: EntityRenderState,
        partialTick: Float,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        packedLight: Int
    ): Boolean {
        val cap = ProjectileCapability[projectile]
        if (cap != null && cap.isModelInitialized() && cap.isModelReady()) {
            RendererManager.getProjectileRenderer()
                .render(cap.getEntity(), state, partialTick, poseStack, multiBufferSource, packedLight)
            return false
        }
        return true
    }
}