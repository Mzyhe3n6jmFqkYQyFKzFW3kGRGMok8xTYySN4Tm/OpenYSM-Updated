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
            it.getAnimatableConsumer().accept(this)
            expressionBuilder = getAnimationData().getAnimationControllerByName(VehicleAnimationController.ORIGIN_CONTROLLER_KEY) as? VehicleRotationController
        }
    }

    open fun getExpressionOffset(): Vector3f? {
        return expressionBuilder?.getVehicleRotation()
    }

    override fun buildRenderShape(modelAssembly: ModelAssembly, isDefault: Boolean): ModelWrapper? {
        val key = entity.type.builtInRegistryHolder().key().identifier()
        val modelBundle = modelAssembly.getVehicleModels()[key]
        if (modelBundle != null) {
            return EntityModelWrapper(modelAssembly, isDefault, modelBundle)
        }
        return null
    }

    override fun onModelLoaded(modelAssembly: ModelAssembly) {
        super.onModelLoaded(modelAssembly)
        val key = entity.type.builtInRegistryHolder().key().identifier()
        vehicleModel = modelAssembly.getVehicleModels()[key]
    }

    override fun clearModel() {
        super.clearModel()
        vehicleModel = null
        expressionBuilder = null
    }

    override fun getAnimationProcessor(): GeoModel {
        return vehicleModel!!.getModel()
    }

    override fun getTextureLocation(): Identifier {
        return (getRenderShape() as EntityModelWrapper).textureLocatable.getResourceLocation().orElseGet(MissingTextureAtlasSprite::getLocation)
    }

    override fun getAnimation(str: String): Animation? {
        return vehicleModel?.getAnimations()?.get(str)
    }

    override fun getAnimationEntries(str: String): AnimationController? {
        return vehicleModel?.getAnimationControllers()?.get(str)
    }

    override fun isModelReady(): Boolean {
        return super.isModelReady() && vehicleModel != null && (getRenderShape()?.isValid() == true)
    }

    override fun getHeightScale(): Float {
        return 0.7f
    }

    override fun getWidthScale(): Float {
        return 0.7f
    }

    private class EntityModelWrapper(
        modelAssembly: ModelAssembly,
        isDefault: Boolean,
        modelBundle: VehicleModelBundle
    ) : ModelWrapper(modelAssembly, isDefault) {
        val textureLocatable: IResourceLocatable = UploadManager.getOrCreateLocatable(modelBundle.getTexture(), true)

        override fun isValid(): Boolean {
            return textureLocatable.getResourceLocation().isPresent
        }
    }
}