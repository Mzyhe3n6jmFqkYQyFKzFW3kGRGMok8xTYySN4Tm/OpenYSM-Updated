package rip.ysm.api.client

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.world.entity.LivingEntity
import rip.ysm.api.client.fabric.RenderLivingBridgeImpl

object RenderLivingBridge {
    fun firePre(
        entity: LivingEntity,
        renderer: LivingEntityRenderer<*, *, *>,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int
    ): Boolean = RenderLivingBridgeImpl.firePre(entity, renderer, partialTick, poseStack, bufferSource, packedLight)

    fun firePost(
        entity: LivingEntity,
        renderer: LivingEntityRenderer<*, *, *>,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int
    ) {
        RenderLivingBridgeImpl.firePost(entity, renderer, partialTick, poseStack, bufferSource, packedLight)
    }
}
