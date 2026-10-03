package com.elfmcys.yesstevemodel.client.model

import com.elfmcys.yesstevemodel.client.entity.GeckoProjectileEntity
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.controller.controllers.ProjectileAnimationController
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap
import net.minecraft.client.renderer.texture.AbstractTexture
import java.util.function.Consumer

open class ProjectileModelBundle(
    val model: GeoModel,
    val animations: Object2ReferenceMap<String, Animation>,
    val animationControllers: Object2ReferenceMap<String, AnimationController>,
    val texture: AbstractTexture,
    resourceBundle: ModelResourceBundle
) {
    // TODO: Replace Consumer
    val controllerInitializer: Consumer<GeckoProjectileEntity> =
        ProjectileAnimationController.buildControllers(this, resourceBundle)
}