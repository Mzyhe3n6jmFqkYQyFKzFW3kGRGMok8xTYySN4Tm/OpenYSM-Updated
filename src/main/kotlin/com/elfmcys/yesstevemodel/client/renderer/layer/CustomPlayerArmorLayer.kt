package com.elfmcys.yesstevemodel.client.renderer.layer

import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.client.renderer.RenderContext
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoLayerRenderer
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel
import com.elfmcys.yesstevemodel.geckolib3.util.RenderUtils
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.ItemInHandRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import rip.ysm.compat.simplehats.SimpleHatsHelper

open class CustomPlayerArmorLayer(context: EntityRendererProvider.Context) : GeoLayerRenderer<CustomPlayerEntity>() {
    private val itemRenderer: ItemInHandRenderer = Minecraft.getInstance().gameRenderer.itemInHandRenderer

    override fun render(
        state: AvatarRenderState,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        packedLightIn: Int,
        entityLivingBaseIn: CustomPlayerEntity,
        limbSwing: Float,
        limbSwingAmount: Float,
        partialTick: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        val player = entityLivingBaseIn.entity
        val model = entityLivingBaseIn.currentModel
        if (model != null && model.headBones().isNotEmpty()) {
            val itemBySlot = player.getItemBySlot(EquipmentSlot.HEAD)
            if (!itemBySlot.isEmpty && !isArmorItem(itemBySlot))
                renderArmorPiece(poseStack, multiBufferSource, packedLightIn, model, player, itemBySlot)
            val stack = SimpleHatsHelper.getHatItem(player)
            if (!stack.isEmpty) renderArmorPiece(poseStack, multiBufferSource, packedLightIn, model, player, stack)
        }
    }

    private fun isArmorItem(stack: ItemStack): Boolean {
        val equippable = stack.get(DataComponents.EQUIPPABLE)
        return equippable != null && equippable.slot() == EquipmentSlot.HEAD
    }

    private fun renderArmorPiece(
        poseStack: PoseStack,
        bufferSource: MultiBufferSource,
        i: Int,
        model: AnimatedGeoModel,
        player: Player,
        stack: ItemStack
    ) {
        val collector = RenderContext.collector() ?: return
        poseStack.pushPose()
        RenderUtils.prepMatrixForLocator(poseStack, model.headBones())
        poseStack.scale(0.625f, 0.625f, 0.625f)
        poseStack.translate(0.0f, 0.25f, 0.0f)
        itemRenderer.renderItem(player, stack, ItemDisplayContext.HEAD, poseStack, collector, i)
        poseStack.popPose()
    }
}