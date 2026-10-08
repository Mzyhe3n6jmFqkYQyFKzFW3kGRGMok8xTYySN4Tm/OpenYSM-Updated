package com.elfmcys.yesstevemodel.client.entity

import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.client.model.ProjectileModelBundle
import com.elfmcys.yesstevemodel.client.upload.UploadManager
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.projectile.Projectile

open class GeckoProjectileEntity(
    projectile: Projectile
) : GeoEntity<Projectile>(projectile, true) {
    private var projectileModelContext: ProjectileModelBundle? = null

    override fun registerAnimationControllers() {
        projectileModelContext?.controllerInitializer(this)
    }

    override fun buildRenderShape(modelAssembly: ModelAssembly, isDefault: Boolean): ModelWrapper? {
        if (!isDefault) {
            val key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.type)
            val modelBundle = modelAssembly.projectileModels[key]
            if (modelBundle != null) return ProjectileModelWrapper(modelAssembly, false, modelBundle)
        }
        return null
    }

    override fun onModelLoaded(modelAssembly: ModelAssembly) {
        super.onModelLoaded(modelAssembly)
        val key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.type)
        projectileModelContext = modelAssembly.projectileModels[key]
    }

    override fun clearModel() {
        super.clearModel()
        projectileModelContext = null
    }

    override val model: GeoModel?
        get() = projectileModelContext?.model

    override val textureLocation: Identifier
        get() = (renderShape as ProjectileModelWrapper).textureLocatable.getResourceLocation()
            ?: MissingTextureAtlasSprite.getLocation()

    override fun getAnimation(str: String): Animation? = projectileModelContext?.animations?.get(str)

    override fun getAnimationEntries(str: String): AnimationController? =
        projectileModelContext?.animationControllers?.get(str)

    override val isModelReady: Boolean
        get() = super.isModelReady && projectileModelContext != null && renderShape?.isValid == true

    override val heightScale: Float
        get() = 0.7f

    override val widthScale: Float
        get() = 0.7f

    private class ProjectileModelWrapper(
        modelAssembly: ModelAssembly,
        isDefault: Boolean,
        modelBundle: ProjectileModelBundle
    ) : ModelWrapper(modelAssembly, isDefault) {
        val textureLocatable = UploadManager.getOrCreateLocatable(modelBundle.texture, true)

        override val isValid: Boolean
            get() = textureLocatable.getResourceLocation() != null
    }
}