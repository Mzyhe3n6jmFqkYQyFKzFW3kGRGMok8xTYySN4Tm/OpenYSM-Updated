package com.elfmcys.yesstevemodel.client.renderer.layer;

import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.renderer.RenderContext;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoLayerRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import rip.ysm.compat.carryon.CarryOnCompat;
import rip.ysm.compat.carryon.CarryOnDataHelper;
import rip.ysm.compat.carryon.CarryOnRenderer;

public class CustomPlayerCarryOnLayer extends GeoLayerRenderer<CustomPlayerEntity> {

    @Override
    public void render(
            AvatarRenderState state,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLightIn,
            CustomPlayerEntity entityLivingBaseIn,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        if (!CarryOnCompat.isLoaded()) {
            return;
        }
        LivingEntity entity = entityLivingBaseIn.getEntity();
        if (!(entity instanceof Player player)) {
            return;
        }
        if (!CarryOnDataHelper.isPlayerCarrying(player)) {
            return;
        }
        SubmitNodeCollector collector = RenderContext.collector();
        if (collector == null) {
            return;
        }
        CarryOnRenderer.render(player, poseStack, packedLightIn, partialTick, collector);
    }
}
