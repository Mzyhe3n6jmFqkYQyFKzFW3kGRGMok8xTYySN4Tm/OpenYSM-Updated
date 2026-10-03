package com.elfmcys.yesstevemodel.client.model

import com.elfmcys.yesstevemodel.client.entity.GeckoVehicleEntity
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.controller.controllers.VehicleAnimationController
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap
import net.minecraft.client.renderer.texture.AbstractTexture

open class VehicleModelBundle(
    val model: GeoModel,
    val animations: Object2ReferenceMap<String, Animation>,
    val animationControllers: Object2ReferenceMap<String, AnimationController>,
    val texture: AbstractTexture,
    modelResourceBundle: ModelResourceBundle
) {
    val controllerInitializer: (GeckoVehicleEntity) -> Unit =
        VehicleAnimationController.buildControllers(this, modelResourceBundle)
}