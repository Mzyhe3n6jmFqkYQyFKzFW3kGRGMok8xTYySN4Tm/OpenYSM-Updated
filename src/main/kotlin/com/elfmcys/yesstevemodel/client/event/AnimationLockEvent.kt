package com.elfmcys.yesstevemodel.client.event

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.client.input.AnimationRouletteKey
import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.network.message.C2SPlayAnimationPacket
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import rip.ysm.api.client.event.ClientRawInputEvent
import rip.ysm.api.event.EventResult
import kotlin.math.abs

object AnimationLockEvent {
    @JvmField
    var animationLocked: Boolean = false

    @JvmStatic
    fun register() {
        ClientRawInputEvent.KEY_PRESSED.register { _, action, event ->
            if (YesSteveModel.isAvailable() && action == 1 && AnimationRouletteKey.KEY_LOCK.matches(event))
                animationLocked = !animationLocked
            EventResult.pass()
        }
        ClientTickEvents.END_CLIENT_TICK.register(::onClientTick)
    }

    @JvmStatic
    fun onClientTick(client: Minecraft) {
        val localPlayer = client.player
        if (YesSteveModel.isAvailable() && !animationLocked && localPlayer != null && isPlayerMoving(localPlayer)) {
            PlayerCapability[localPlayer]?.let { cap ->
                if (cap.isModelSwitching()) {
                    cap.clearModelSwitch()
                    if (NetworkHandler.isClientConnected()) {
                        NetworkHandler.sendToServer(C2SPlayAnimationPacket.createDefault())
                    }
                }
            }
        }
    }

    @JvmStatic
    fun isPlayerMoving(localPlayer: LocalPlayer): Boolean {
        val input = localPlayer.input
        val move = input.getMoveVector()
        return isSignificantImpulse(move.x) || isSignificantImpulse(move.y) || input.keyPresses.jump() || input.keyPresses.shift()
    }

    private fun isSignificantImpulse(impulse: Float): Boolean = abs(impulse) > 1.0E-5f

    @JvmStatic
    fun toggleLock() {
        animationLocked = !animationLocked
    }

    @JvmStatic
    fun isLocked(): Boolean = animationLocked
}
