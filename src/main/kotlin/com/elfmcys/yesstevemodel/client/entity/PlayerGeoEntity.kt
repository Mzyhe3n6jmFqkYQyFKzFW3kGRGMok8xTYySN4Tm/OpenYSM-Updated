package com.elfmcys.yesstevemodel.client.entity

import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.client.animation.condition.ArmorConditions
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel
import net.minecraft.client.player.LocalPlayer
import net.minecraft.resources.Identifier

open class PlayerGeoEntity(
    player: LocalPlayer,
    val playerCapability: PlayerCapability
) : GeoEntity<LocalPlayer>(player, false) {

    init {
        setModelId(playerCapability.getModelId())
    }

    override fun registerAnimationControllers() {
        getModelAssembly()?.animationBundle?.armControllerInstaller?.accept(this)
    }

    override fun shouldSkipAnimation(event: AnimationEvent<*>): Boolean {
        return true
    }

    override fun tickModel() {
        if (playerCapability.getModelAssembly() != getModelAssembly()) {
            setModelId(playerCapability.getModelId())
        }
    }

    override fun buildRenderShape(modelAssembly: ModelAssembly, isDefault: Boolean): ModelWrapper? {
        return playerCapability.getRenderShape()
    }

    override fun getAnimationEntries(str: String): AnimationController? {
        return getModelAssembly()?.animationBundle?.animationEntries?.get(str)
    }

    override fun getTextureLocation(): Identifier {
        return playerCapability.getTextureLocation()
    }

    override fun getHeightScale(): Float {
        return getModelAssembly()?.modelData?.modelProperties?.heightScale ?: 1.0f
    }

    override fun getWidthScale(): Float {
        return getModelAssembly()?.modelData?.modelProperties?.widthScale ?: 1.0f
    }

    override fun getAnimation(str: String): Animation? {
        return getModelAssembly()?.animationBundle?.armAnimations?.get(str)
    }

    open fun getArmModelProcessor(): ArmorConditions? {
        return getModelAssembly()?.animationBundle?.modelProcessor
    }

    override fun getAnimationProcessor(): GeoModel {
        return getModelAssembly()!!.animationBundle.armModel
    }

    override fun setupAnim(seekTime: Float, isFirstPerson: Boolean) {
        getEvaluationContext().setRoamingProperties(playerCapability.getServerVarContainer())
    }
}