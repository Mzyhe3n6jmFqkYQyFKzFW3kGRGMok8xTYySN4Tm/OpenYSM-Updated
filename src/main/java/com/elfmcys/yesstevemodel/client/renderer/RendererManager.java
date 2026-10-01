package com.elfmcys.yesstevemodel.client.renderer;

import com.elfmcys.yesstevemodel.NameSpaces;
import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.mixin.client.EntityRenderDispatcherAccessor;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import rip.ysm.api.PlatformAPI;
import rip.ysm.compat.sbackpack.SBackpackCompat;

public class RendererManager {

    private static CustomPlayerRenderer playerRenderer;

    private static ProjectileRenderer projectileRenderer;

    private static HandItemRenderer handRenderer;

    private static VehicleRenderer vehicleRenderer;

    private RendererManager() {
    }

    public static void register() {
        if (PlatformAPI.isServer()) {
            return;
        }
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return NameSpaces.MOD.path("renderer_manager");
            }

            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                resetRenderers();
            }
        });
    }

    private static void resetRenderers() {
        playerRenderer = null;
        projectileRenderer = null;
        handRenderer = null;
        vehicleRenderer = null;
    }

    private static void initRenderers(ResourceManager resourceManager) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRendererProvider.Context context = new EntityRendererProvider.Context(
                entityRenderDispatcher,
                Minecraft.getInstance().getItemModelResolver(),
                Minecraft.getInstance().getMapRenderer(),
                Minecraft.getInstance().getBlockRenderer(),
                resourceManager,
                Minecraft.getInstance().getEntityModels(),
                ((EntityRenderDispatcherAccessor) Minecraft.getInstance().getEntityRenderDispatcher()).getEquipmentAssetManager(),
                Minecraft.getInstance().getAtlasManager(),
                Minecraft.getInstance().font,
                Minecraft.getInstance().playerSkinRenderCache()
        );
        playerRenderer = new CustomPlayerRenderer(context);
        projectileRenderer = new ProjectileRenderer(context);
        handRenderer = new HandItemRenderer();
        vehicleRenderer = new VehicleRenderer(context);
        SBackpackCompat.setupRenderLayers();
    }

    public static CustomPlayerRenderer getPlayerRenderer() {
        if (playerRenderer == null) {
            initRenderers(Minecraft.getInstance().getResourceManager());
        }
        return playerRenderer;
    }

    public static ProjectileRenderer getProjectileRenderer() {
        if (projectileRenderer == null) {
            initRenderers(Minecraft.getInstance().getResourceManager());
        }
        return projectileRenderer;
    }

    public static HandItemRenderer getHandRenderer() {
        if (handRenderer == null) {
            initRenderers(Minecraft.getInstance().getResourceManager());
        }
        return handRenderer;
    }

    public static VehicleRenderer getVehicleRenderer() {
        if (vehicleRenderer == null) {
            initRenderers(Minecraft.getInstance().getResourceManager());
        }
        return vehicleRenderer;
    }
}
