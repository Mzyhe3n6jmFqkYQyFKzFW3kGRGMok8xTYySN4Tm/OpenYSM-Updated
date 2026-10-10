package rip.ysm.compat.slashblade.fabric

import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import rip.ysm.compat.ModCompat

object SlashBladeRendererImpl : ModCompat("slashblade") {
    fun renderOnEntity(
        livingEntity: LivingEntity,
        model: AnimatedGeoModel,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int,
        stack: ItemStack,
        partialTick: Float
    ) {
    }

    fun renderRightWaist(
        model: AnimatedGeoModel,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int,
        stack: ItemStack
    ) {
    }
}
