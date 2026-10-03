package com.elfmcys.yesstevemodel.client.model

import com.elfmcys.yesstevemodel.client.animation.condition.ArmorConditions
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionManager
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.client.entity.PlayerGeoEntity
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.controller.controllers.FirstPersonArmAnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.controller.controllers.PlayerAnimationController
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap
import net.minecraft.client.renderer.texture.AbstractTexture
import rip.ysm.compat.touhoulittlemaid.TouhouLittleMaidCompat
import java.util.function.Consumer

open class PlayerModelBundle(
    val mainModel: GeoModel,
    val armModel: GeoModel,
    val mainAnimations: Object2ReferenceMap<String, Animation>,
    val armAnimations: Object2ReferenceMap<String, Animation>,
    val conditionManager: ConditionManager,
    val modelProcessor: ArmorConditions,
    val animationEntries: Object2ReferenceMap<String, AnimationController>,
    val textures: OrderedStringMap<String, out AbstractTexture>,
    val defaultTextureName: String,
    val defaultTexture: AbstractTexture?,
    modelResourceBundle: ModelResourceBundle
) {
    val playerControllerInstaller: Consumer<CustomPlayerEntity>? = PlayerAnimationController.buildControllers(this, modelResourceBundle)
    val armControllerInstaller: Consumer<PlayerGeoEntity>? = FirstPersonArmAnimationController.buildControllers(this, modelResourceBundle)
    val maidControllerInstaller: Any? = TouhouLittleMaidCompat.buildControllers(this, modelResourceBundle)
}