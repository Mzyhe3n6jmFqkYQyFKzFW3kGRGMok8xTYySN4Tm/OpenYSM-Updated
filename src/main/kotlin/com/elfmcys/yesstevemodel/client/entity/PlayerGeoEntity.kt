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
    private val playerCapability: PlayerCapability
) : GeoEntity<LocalPlayer>(player, false) {
    init {
        modelId = playerCapability.modelId
    }

    override fun registerAnimationControllers() {
        modelAssembly?.animationBundle?.armControllerInstaller?.invoke(this)
    }

    override fun shouldSkipAnimation(event: AnimationEvent<*>): Boolean {
        return true
    }

    override fun tickModel() {
        if (playerCapability.modelAssembly != modelAssembly) {
            modelId = playerCapability.modelId
        }
    }

    override fun buildRenderShape(modelAssembly: ModelAssembly, isDefault: Boolean): ModelWrapper? =
        playerCapability.renderShape

    override fun getAnimationEntries(str: String): AnimationController? {
        return modelAssembly?.animationBundle?.animationEntries?.get(str)
    }

    override val textureLocation: Identifier
        get() {
            return playerCapability.textureLocation
        }

    override val heightScale: Float
        get() {
            return modelAssembly?.modelData?.modelProperties?.heightScale ?: 1.0f
        }

    override val widthScale: Float
        get() {
            return modelAssembly?.modelData?.modelProperties?.widthScale ?: 1.0f
        }

    override fun getAnimation(str: String): Animation? {
        return modelAssembly?.animationBundle?.armAnimations?.get(str)
    }

    open fun getArmModelProcessor(): ArmorConditions? {
        return modelAssembly?.animationBundle?.modelProcessor
    }

    override fun getAnimationProcessor(): GeoModel {
        return modelAssembly!!.animationBundle.armModel
    }

    override fun setupAnim(seekTime: Float, isFirstPerson: Boolean) {
        getEvaluationContext().setRoamingProperties(playerCapability.serverVarContainer)
    }
}