package com.elfmcys.yesstevemodel.client.renderer.layer

import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.client.renderer.RenderContext
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoLayerRenderer
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel
import com.elfmcys.yesstevemodel.geckolib3.util.RenderUtils
import com.elfmcys.yesstevemodel.util.accessors.BufferSourceAccessor
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.ItemInHandRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.world.entity.HumanoidArm
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import rip.ysm.compat.gun.swarfare.SWarfareCompat
import rip.ysm.compat.gun.tacz.TacCompat
import rip.ysm.compat.slashblade.SlashBladeCompat
import rip.ysm.compat.slashblade.SlashBladeRenderer

open class CustomPlayerItemInHandLayer(
    private val itemRenderer: ItemInHandRenderer
) : GeoLayerRenderer<CustomPlayerEntity>() {
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
        val entity: LivingEntity = entityLivingBaseIn.entity
        val animatedGeoModel: AnimatedGeoModel = entityLivingBaseIn.currentModel ?: return
        val offhandItem: ItemStack = entity.offhandItem
        val mainHandItem: ItemStack = entity.mainHandItem
        if (!offhandItem.isEmpty || !mainHandItem.isEmpty) {
            poseStack.pushPose()
            val useExtraPlayer: Boolean = entityLivingBaseIn.isRenderLayersFirst
            if (animatedGeoModel.rightHandBones().isNotEmpty()) {
                if (SlashBladeCompat.isSlashBladeItem(mainHandItem)) {
                    SlashBladeRenderer.renderOnEntity(
                        entity,
                        animatedGeoModel,
                        poseStack,
                        multiBufferSource,
                        packedLightIn,
                        mainHandItem,
                        partialTick
                    )
                } else {
                    TacCompat.handleGunSound(entity, mainHandItem)
                    renderItem(
                        animatedGeoModel,
                        entity,
                        mainHandItem,
                        ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                        HumanoidArm.RIGHT,
                        poseStack,
                        multiBufferSource,
                        packedLightIn
                    )
                    if (useExtraPlayer && !mainHandItem.isEmpty && multiBufferSource is BufferSourceAccessor) {
                        (multiBufferSource as BufferSourceAccessor).`ysm$initialize`()
                    }
                    TacCompat.handleItemSound(mainHandItem)
                }
            }
            if (animatedGeoModel.leftHandBones().isNotEmpty()) {
                if (SlashBladeCompat.isSlashBladeItem(offhandItem)) {
                    SlashBladeRenderer.renderRightWaist(
                        animatedGeoModel,
                        poseStack,
                        multiBufferSource,
                        packedLightIn,
                        offhandItem
                    )
                } else {
                    if (!SWarfareCompat.isGunItem(offhandItem)) {
                        renderItem(
                            animatedGeoModel,
                            entity,
                            offhandItem,
                            ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                            HumanoidArm.LEFT,
                            poseStack,
                            multiBufferSource,
                            packedLightIn
                        )
                    }
                    if (useExtraPlayer && !offhandItem.isEmpty && multiBufferSource is BufferSourceAccessor) {
                        (multiBufferSource as BufferSourceAccessor).`ysm$initialize`()
                    }
                }
            }
            poseStack.popPose()
            TacCompat.applyItemTransform(offhandItem, animatedGeoModel, entity, poseStack, packedLightIn, partialTick)
            SWarfareCompat.applyGunTransform(
                offhandItem,
                animatedGeoModel,
                entity,
                poseStack,
                packedLightIn,
                partialTick
            )
        }
    }

    open fun renderItem(
        model: AnimatedGeoModel,
        livingEntity: LivingEntity,
        itemStack: ItemStack,
        itemDisplayContext: ItemDisplayContext,
        humanoidArm: HumanoidArm,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        i: Int
    ) {
        if (!itemStack.isEmpty) {
            val collector = RenderContext.collector() ?: return
            val isLeftHand: Boolean = humanoidArm == HumanoidArm.LEFT
            poseStack.pushPose()
            if (!applyItemBoneTransform(humanoidArm, poseStack, model)) {
                poseStack.translate(0.0, -0.0625, -0.1)
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f))
                if (SWarfareCompat.isGunItem(itemStack)) {
                    poseStack.translate(0.1, 0.0, 0.0)
                    poseStack.scale(1.25f, 1.25f, 1.25f)
                }
                itemRenderer.renderItem(livingEntity, itemStack, itemDisplayContext, poseStack, collector, i)
            }
            poseStack.popPose()
            val chains = if (isLeftHand) model.rightHandChain() else model.leftHandChains()
            chains.forEach { list ->
                poseStack.pushPose()
                if (!RenderUtils.prepMatrixForLocator(poseStack, list)) {
                    poseStack.translate(0.0, -0.0625, -0.1)
                    poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f))
                    if (SWarfareCompat.isGunItem(itemStack)) {
                        poseStack.scale(1.25f, 1.25f, 1.25f)
                    }
                    itemRenderer.renderItem(livingEntity, itemStack, itemDisplayContext, poseStack, collector, i)
                }
                poseStack.popPose()
            }
        }
    }

    open fun applyItemBoneTransform(humanoidArm: HumanoidArm, poseStack: PoseStack, model: AnimatedGeoModel): Boolean {
        if (humanoidArm == HumanoidArm.LEFT) {
            return RenderUtils.prepMatrixForLocator(poseStack, model.leftHandBones())
        }
        return RenderUtils.prepMatrixForLocator(poseStack, model.rightHandBones())
    }
}