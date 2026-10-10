package rip.ysm.compat.slashblade

import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import rip.ysm.compat.ModCompat
import rip.ysm.compat.slashblade.fabric.SlashBladeRendererImpl

object SlashBladeRenderer : ModCompat("slashblade") {
    fun renderOnEntity(
        livingEntity: LivingEntity,
        model: AnimatedGeoModel,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int,
        stack: ItemStack,
        partialTick: Float
    ) {
        if (!isModLoaded) return
        SlashBladeRendererImpl.renderOnEntity(
            livingEntity,
            model,
            poseStack,
            bufferSource,
            packedLight,
            stack,
            partialTick
        )
    }

    fun renderRightWaist(
        model: AnimatedGeoModel,
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        packedLight: Int,
        stack: ItemStack
    ) {
        if (!isModLoaded) return
        SlashBladeRendererImpl.renderRightWaist(model, poseStack, bufferSource, packedLight, stack)
    }
}
