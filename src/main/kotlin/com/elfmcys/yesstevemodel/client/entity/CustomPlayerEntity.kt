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
    @JvmField val isLocalPlayer: Boolean,
    isActive: Boolean
) : LivingAnimatable<Player>(player, isActive), RoamingPropertyHolder {
    @JvmField
    var isModelSwitching: Boolean = false

    @JvmField
    var selectedModelId: String = "idle"

    @JvmField
    var isDisabled: Boolean = false
    private var syncIValues: List<IValue>? = null

    init {
        if (player is LocalPlayer) {
            markModelInitialized()
        }
    }

    override fun registerAnimationControllers() {
        getModelAssembly()?.animationBundle?.playerControllerInstaller?.invoke(this)
    }

    override fun resetModel() {
        super.resetModel()
        syncIValues = null
    }

    override fun reset() {
        super.reset()
        isModelSwitching = false
        selectedModelId = "idle"
        isDisabled = false
    }

    override fun shouldSkipAnimation(event: AnimationEvent<*>): Boolean {
        return event.isFirstPerson() || (!isLocalPlayer && OculusCompat.isPBRActive())
    }

    override fun getServerVarContainer(): Struct? {
        return null
    }

    open fun isLocalPlayerModel(): Boolean {
        return isLocalPlayer
    }

    override fun onModelLoaded(context: ModelAssembly) {
        super.onModelLoaded(context)
        syncIValues = context.expressionCache.events[MolangEventDispatcher.SYNC]
    }

    open fun requestModelSwitch(str: String) {
        if (getAnimation(str) != null) {
            selectedModelId = str
            isModelSwitching = true
            isDisabled = true
            return
        }
        isModelSwitching = false
    }

    open fun enableModel() {
        isDisabled = false
    }

    open fun isModelSwitching(): Boolean {
        return isModelSwitching
    }

    open fun isDisabledState(): Boolean {
        return isDisabled
    }

    open fun getSelectedModelId(): String {
        return selectedModelId
    }

    open fun clearModelSwitch() {
        isModelSwitching = false
    }

    override fun setupAnim(seekTime: Float, isFirstPerson: Boolean) {
        super.setupAnim(seekTime, isFirstPerson)
        getEvaluationContext().setRoamingProperties(getServerVarContainer())
    }

    override fun afterSetupAnim(seekTime: Float, isFirstPerson: Boolean) {
        super.afterSetupAnim(seekTime, isFirstPerson)
        if (isLocalPlayer && isFirstPerson && isModelSwitching() && getAnimationState(PlayerAnimationController.CAP_CONTROLLER_KEY) == AnimationState.IDLE) {
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