package com.elfmcys.yesstevemodel.client.renderer

import com.elfmcys.yesstevemodel.NameSpaces
import com.elfmcys.yesstevemodel.YesSteveModel
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.resource.v1.ResourceLoader
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import rip.ysm.compat.sbackpack.SBackpackCompat

@Environment(EnvType.CLIENT)
object RendererManager {
    private var playerRenderer2: CustomPlayerRenderer? = null
    private var projectileRenderer2: ProjectileRenderer? = null
    private var handRenderer2: HandItemRenderer? = null
    private var vehicleRenderer2: VehicleRenderer? = null

    init {
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(
            NameSpaces.MOD.path("renderer_manager"),
            ResourceManagerReloadListener {
                resetRenderers()
            }
        )
    }

    private fun resetRenderers() {
        playerRenderer2 = null
        projectileRenderer2 = null
        handRenderer2 = null
        vehicleRenderer2 = null
    }

    private fun initRenderers(resourceManager: ResourceManager) {
        if (!YesSteveModel.isAvailable) return
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
        playerRenderer2 = CustomPlayerRenderer(context)
        projectileRenderer2 = ProjectileRenderer(context)
        handRenderer2 = HandItemRenderer()
        vehicleRenderer2 = VehicleRenderer(context)
        SBackpackCompat.setupRenderLayers()
    }

    @JvmStatic
    val playerRenderer: CustomPlayerRenderer
        get() {
            val current = playerRenderer2
            if (current != null) return current
            initRenderers(Minecraft.getInstance().resourceManager)
            return playerRenderer2!!
        }

    @JvmStatic
    val projectileRenderer: ProjectileRenderer
        get() {
            val current = projectileRenderer2
            if (current != null) return current
            initRenderers(Minecraft.getInstance().resourceManager)
            return projectileRenderer2!!
        }

    @JvmStatic
    val handRenderer: HandItemRenderer
        get() {
            val current = handRenderer2
            if (current != null) return current
            initRenderers(Minecraft.getInstance().resourceManager)
            return handRenderer2!!
        }

    @JvmStatic
    val vehicleRenderer: VehicleRenderer
        get() {
            val current = vehicleRenderer2
            if (current != null) return current
            initRenderers(Minecraft.getInstance().resourceManager)
            return vehicleRenderer2!!
        }
}