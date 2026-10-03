package com.elfmcys.yesstevemodel.client.entity

import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.client.model.VehicleModelBundle
import com.elfmcys.yesstevemodel.client.upload.IResourceLocatable
import com.elfmcys.yesstevemodel.client.upload.UploadManager
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.controller.controllers.VehicleAnimationController
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity
import org.joml.Vector3f

open class GeckoVehicleEntity(
    entity: Entity
) : GeoEntity<Entity>(entity, true) {
    private var vehicleModel: VehicleModelBundle? = null
    private var expressionBuilder: VehicleRotationController? = null

    override fun registerAnimationControllers() {
        vehicleModel?.let {
            it.controllerInitializer(this)
            expressionBuilder =
                getAnimationData().getAnimationControllerByName(VehicleAnimationController.ORIGIN_CONTROLLER_KEY) as? VehicleRotationController
        }
    }

    open fun getExpressionOffset(): Vector3f? = expressionBuilder?.getVehicleRotation()

    // TODO: 'fun builtInRegistryHolder(): Holder.Reference<EntityType<*>>' is deprecated. Deprecated in Java.
    override fun buildRenderShape(modelAssembly: ModelAssembly, isDefault: Boolean): ModelWrapper? {
        val key = entity.type.builtInRegistryHolder().key().identifier()
        val modelBundle = modelAssembly.vehicleModels[key]
        if (modelBundle != null) return EntityModelWrapper(modelAssembly, isDefault, modelBundle)
        return null
    }

    override fun onModelLoaded(modelAssembly: ModelAssembly) {
        super.onModelLoaded(modelAssembly)
        val key = entity.type.builtInRegistryHolder().key().identifier()
        vehicleModel = modelAssembly.vehicleModels[key]
    }

    override fun clearModel() {
        super.clearModel()
        vehicleModel = null
        expressionBuilder = null
    }

    override fun getAnimationProcessor(): GeoModel = vehicleModel!!.model

    override fun getTextureLocation(): Identifier =
        (getRenderShape() as EntityModelWrapper).textureLocatable.getResourceLocation()
            ?: MissingTextureAtlasSprite.getLocation()

    override fun getAnimation(str: String): Animation? = vehicleModel?.animations?.get(str)

    override fun getAnimationEntries(str: String): AnimationController? = vehicleModel?.animationControllers?.get(str)

    override fun isModelReady(): Boolean =
        super.isModelReady() && vehicleModel != null && (getRenderShape()?.isValid() == true)

    override fun getHeightScale(): Float = 0.7f

    override fun getWidthScale(): Float = 0.7f

    private class EntityModelWrapper(
        modelAssembly: ModelAssembly,
        isDefault: Boolean,
        modelBundle: VehicleModelBundle
    ) : ModelWrapper(modelAssembly, isDefault) {
        val textureLocatable: IResourceLocatable = UploadManager.getOrCreateLocatable(modelBundle.texture, true)

        override fun isValid(): Boolean = textureLocatable.getResourceLocation() != null
    }
}