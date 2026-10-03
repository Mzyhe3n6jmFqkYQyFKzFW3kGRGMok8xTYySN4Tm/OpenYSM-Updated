package com.elfmcys.yesstevemodel.client.renderer

import com.elfmcys.yesstevemodel.NameSpaces
import com.elfmcys.yesstevemodel.YesSteveModel
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import rip.ysm.api.PlatformAPI
import rip.ysm.compat.sbackpack.SBackpackCompat

object RendererManager {
    @JvmField
    var playerRenderer: CustomPlayerRenderer? = null

    @JvmField
    var projectileRenderer: ProjectileRenderer? = null

    @JvmField
    var handRenderer: HandItemRenderer? = null

    @JvmField
    var vehicleRenderer: VehicleRenderer? = null

    @JvmStatic
    fun register() {
        if (PlatformAPI.isServer()) return
        // TODO: interface ResourceManagerHelper : Any' is deprecated. Deprecated in Java.
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
            .registerReloadListener(object : SimpleSynchronousResourceReloadListener {
                override fun getFabricId(): Identifier =
                    Identifier.fromNamespaceAndPath(NameSpaces.MOD(), "renderer_manager")

                override fun onResourceManagerReload(resourceManager: ResourceManager) {
                    resetRenderers()
                }
            })
    }

    @JvmStatic
    fun resetRenderers() {
        playerRenderer = null
        projectileRenderer = null
        handRenderer = null
        vehicleRenderer = null
    }

    @JvmStatic
    fun initRenderers(resourceManager: ResourceManager) {
        if (!YesSteveModel.isAvailable()) {
            return
        }
        val minecraft = Minecraft.getInstance()
        val entityRenderDispatcher = minecraft.entityRenderDispatcher
        val context = EntityRendererProvider.Context(
            entityRenderDispatcher,
            minecraft.itemModelResolver,
            minecraft.mapRenderer,
            minecraft.blockRenderer,
            resourceManager,
            minecraft.entityModels,
            entityRenderDispatcher.equipmentAssets,
            minecraft.atlasManager,
            minecraft.font,
            minecraft.playerSkinRenderCache()
        )
        playerRenderer = CustomPlayerRenderer(context)
        projectileRenderer = ProjectileRenderer(context)
        handRenderer = HandItemRenderer()
        vehicleRenderer = VehicleRenderer(context)
        SBackpackCompat.setupRenderLayers()
    }

    @JvmStatic
    fun getPlayerRenderer(): CustomPlayerRenderer {
        val current = playerRenderer
        if (current != null) return current
        initRenderers(Minecraft.getInstance().resourceManager)
        return playerRenderer!!
    }

    @JvmStatic
    fun getProjectileRenderer(): ProjectileRenderer {
        val current = projectileRenderer
        if (current != null) return current
        initRenderers(Minecraft.getInstance().resourceManager)
        return projectileRenderer!!
    }

    @JvmStatic
    fun getHandRenderer(): HandItemRenderer {
        val current = handRenderer
        if (current != null) return current
        initRenderers(Minecraft.getInstance().resourceManager)
        return handRenderer!!
    }

    @JvmStatic
    fun getVehicleRenderer(): VehicleRenderer {
        val current = vehicleRenderer
        if (current != null) return current
        initRenderers(Minecraft.getInstance().resourceManager)
        return vehicleRenderer!!
    }
}