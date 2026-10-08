package com.elfmcys.yesstevemodel.client.entity

import com.elfmcys.yesstevemodel.client.animation.molang.MolangEventDispatcher
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.geckolib3.core.controller.controllers.PlayerAnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.enums.AnimationState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.molang.runtime.Struct
import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.network.message.C2SPlayAnimationPacket
import it.unimi.dsi.fastutil.floats.FloatArrayList
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.entity.player.Player
import rip.ysm.compat.oculus.OculusCompat

abstract class CustomPlayerEntity(
    player: Player,
    private val isLocalPlayer: Boolean,
    isActive: Boolean
) : LivingAnimatable<Player>(player, isActive), RoamingPropertyHolder {
    private var isModelSwitching2: Boolean = false
    private var selectedModelId2: String = "idle"
    private var isDisabled: Boolean = false
    private var syncIValues: List<IValue>? = null

    init {
        if (player is LocalPlayer) {
            markModelInitialized()
        }
    }

    override fun registerAnimationControllers() {
        modelAssembly?.animationBundle?.playerControllerInstaller?.invoke(this)
    }

    override fun resetModel() {
        super.resetModel()
        syncIValues = null
    }

    override fun reset() {
        super.reset()
        isModelSwitching2 = false
        selectedModelId2 = "idle"
        isDisabled = false
    }

    override fun shouldSkipAnimation(event: AnimationEvent<*>): Boolean =
        event.isFirstPerson || !isLocalPlayer && OculusCompat.isPBRActive()

    override val serverVarContainer: Struct?
        get() = null

    open val isLocalPlayerModel: Boolean
        get() = isLocalPlayer

    override fun onModelLoaded(modelAssembly: ModelAssembly) {
        super.onModelLoaded(modelAssembly)
        syncIValues = modelAssembly.expressionCache.events[MolangEventDispatcher.SYNC]
    }

    open fun requestModelSwitch(str: String) {
        if (getAnimation(str) != null) {
            selectedModelId2 = str
            isModelSwitching2 = true
            isDisabled = true
            return
        }
        isModelSwitching2 = false
    }

    open fun enableModel() {
        isDisabled = false
    }

    open val isModelSwitching: Boolean
        get() = isModelSwitching2

    open val isDisabledState: Boolean
        get() = isDisabled

    open val selectedModelId: String
        get() = selectedModelId2

    open fun clearModelSwitch() {
        isModelSwitching2 = false
    }

    override fun setupAnim(seekTime: Float, isFirstPerson: Boolean) {
        super.setupAnim(seekTime, isFirstPerson)
        evaluationContext.setRoamingProperties(serverVarContainer)
    }

    override fun afterSetupAnim(seekTime: Float, isFirstPerson: Boolean) {
        super.afterSetupAnim(seekTime, isFirstPerson)
        if (isLocalPlayer && isFirstPerson && isModelSwitching && getAnimationState(PlayerAnimationController.CAP_CONTROLLER_KEY) == AnimationState.IDLE) {
            clearModelSwitch()
            if (NetworkHandler.isClientConnected()) {
                NetworkHandler.sendToServer(C2SPlayAnimationPacket.createDefault())
            }
        }
    }

    open fun executeAnimationExpression(floatArrayList: FloatArrayList) {
        val syncValues = syncIValues
        if (syncValues != null) {
            executeExpression(MolangEventDispatcher.createExpression(syncValues, floatArrayList), true, false, null)
        }
    }
}