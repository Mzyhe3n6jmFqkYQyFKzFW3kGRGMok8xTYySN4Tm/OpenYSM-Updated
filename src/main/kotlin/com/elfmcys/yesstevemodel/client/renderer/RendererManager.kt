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
    private var _playerRenderer: CustomPlayerRenderer? = null
    private var _projectileRenderer: ProjectileRenderer? = null
    private var _handRenderer: HandItemRenderer? = null
    private var _vehicleRenderer: VehicleRenderer? = null

    init {
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(
            NameSpaces.MOD.path("renderer_manager"),
            ResourceManagerReloadListener {
                resetRenderers()
            }
        )
    }

    private fun resetRenderers() {
        _playerRenderer = null
        _projectileRenderer = null
        _handRenderer = null
        _vehicleRenderer = null
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
        _playerRenderer = CustomPlayerRenderer(context)
        _projectileRenderer = ProjectileRenderer(context)
        _handRenderer = HandItemRenderer()
        _vehicleRenderer = VehicleRenderer(context)
        SBackpackCompat.setupRenderLayers()
    }

    @JvmStatic
    val playerRenderer: CustomPlayerRenderer
        get() {
            val current = _playerRenderer
            if (current != null) return current
            initRenderers(Minecraft.getInstance().resourceManager)
            return _playerRenderer!!
        }

    val projectileRenderer: ProjectileRenderer
        get() {
            val current = _projectileRenderer
            if (current != null) return current
            initRenderers(Minecraft.getInstance().resourceManager)
            return _projectileRenderer!!
        }

    @JvmStatic
    val handRenderer: HandItemRenderer
        get() {
            val current = _handRenderer
            if (current != null) return current
            initRenderers(Minecraft.getInstance().resourceManager)
            return _handRenderer!!
        }

    @JvmStatic
    val vehicleRenderer: VehicleRenderer
        get() {
            val current = _vehicleRenderer
            if (current != null) return current
            initRenderers(Minecraft.getInstance().resourceManager)
            return _vehicleRenderer!!
        }
}