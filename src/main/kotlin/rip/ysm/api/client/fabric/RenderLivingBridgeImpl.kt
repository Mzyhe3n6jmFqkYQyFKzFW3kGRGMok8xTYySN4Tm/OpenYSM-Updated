package rip.ysm.api.client.fabric

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.world.entity.LivingEntity

object RenderLivingBridgeImpl {
    @JvmStatic
    fun firePre(
        entity: LivingEntity,
        renderer: LivingEntityRenderer<*, *, *>,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int
    ): Boolean = false

    @JvmStatic
    fun firePost(
        entity: LivingEntity,
        renderer: LivingEntityRenderer<*, *, *>,
        partialTick: Float,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int
    ) {
    }
}
