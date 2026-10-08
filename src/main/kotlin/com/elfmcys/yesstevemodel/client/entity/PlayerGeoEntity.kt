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

    override fun shouldSkipAnimation(event: AnimationEvent<*>): Boolean = true

    override fun tickModel() {
        if (playerCapability.modelAssembly == modelAssembly) return
        modelId = playerCapability.modelId
    }

    override fun buildRenderShape(modelAssembly: ModelAssembly, isDefault: Boolean): ModelWrapper? =
        playerCapability.renderShape

    override fun getAnimationEntries(str: String): AnimationController? =
        modelAssembly?.animationBundle?.animationEntries?.get(str)

    override val textureLocation: Identifier
        get() = playerCapability.textureLocation

    override val heightScale: Float
        get() = modelAssembly?.modelData?.modelProperties?.heightScale ?: 1.0f

    override val widthScale: Float
        get() = modelAssembly?.modelData?.modelProperties?.widthScale ?: 1.0f

    override fun getAnimation(str: String): Animation? {
        return modelAssembly?.animationBundle?.armAnimations?.get(str)
    }

    open val armModelProcessor: ArmorConditions?
        get() = modelAssembly?.animationBundle?.modelProcessor

    override val animationProcessor: GeoModel
        get() {
            return modelAssembly!!.animationBundle.armModel
        }

    override fun setupAnim(seekTime: Float, isFirstPerson: Boolean) {
        getEvaluationContext().setRoamingProperties(playerCapability.serverVarContainer)
    }
}