package com.elfmcys.yesstevemodel.client.entity

import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.client.model.ProjectileModelBundle
import com.elfmcys.yesstevemodel.client.upload.UploadManager
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.projectile.Projectile

open class GeckoProjectileEntity(
    projectile: Projectile
) : GeoEntity<Projectile>(projectile, true) {
    private var projectileModelContext: ProjectileModelBundle? = null

    override fun registerAnimationControllers() {
        projectileModelContext?.controllerInitializer(this)
    }

    // TODO: fun builtInRegistryHolder(): Holder.Reference<EntityType<*>>' is deprecated. Deprecated in Java.
    override fun buildRenderShape(modelAssembly: ModelAssembly, isDefault: Boolean): ModelWrapper? {
        if (!isDefault) {
            val key = entity.type.builtInRegistryHolder().key().identifier()
            val modelBundle = modelAssembly.projectileModels[key]
            if (modelBundle != null) return ProjectileModelWrapper(modelAssembly, false, modelBundle)
        }
        return null
    }

    override fun onModelLoaded(modelAssembly: ModelAssembly) {
        super.onModelLoaded(modelAssembly)
        val key = entity.type.builtInRegistryHolder().key().identifier()
        projectileModelContext = modelAssembly.projectileModels[key]
    }

    override fun clearModel() {
        super.clearModel()
        projectileModelContext = null
    }

    override fun getAnimationProcessor(): GeoModel {
        return projectileModelContext!!.model
    }

    override val textureLocation: Identifier
        get() {
            return (getRenderShape() as ProjectileModelWrapper).textureLocatable.getResourceLocation()
                ?: MissingTextureAtlasSprite.getLocation()
        }

    override fun getAnimation(str: String): Animation? {
        return projectileModelContext?.animations?.get(str)
    }

    override fun getAnimationEntries(str: String): AnimationController? {
        return projectileModelContext?.animationControllers?.get(str)
    }

    override val isModelReady: Boolean
        get() {
            return super.isModelReady && projectileModelContext != null && (getRenderShape()?.isValid == true)
        }

    override val heightScale: Float
        get() {
            return 0.7f
        }

    override val widthScale: Float
        get() {
            return 0.7f
        }

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