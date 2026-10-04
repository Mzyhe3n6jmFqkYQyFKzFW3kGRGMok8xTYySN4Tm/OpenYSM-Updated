package com.elfmcys.yesstevemodel.client.event

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.capability.fabric.client.PlayerCapabilityClientStore
import com.elfmcys.yesstevemodel.event.events.client.ClientPlayerEvent
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.player.LocalPlayer
import rip.ysm.api.capability.CapabilityLifecycle

@Environment(EnvType.CLIENT)
object ClientPlayerCloneEvent {
    init {
        ClientPlayerEvent.CLIENT_PLAYER_RESPAWN.register(::onClientPlayerRespawn)
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(::onClientPlayerQuit)
    }

    @JvmStatic
    fun onClientPlayerRespawn(oldPlayer: LocalPlayer?, newPlayer: LocalPlayer?) {
        if (oldPlayer == null || newPlayer == null || !YesSteveModel.isAvailable()) return
        CapabilityLifecycle.revive(oldPlayer)
        val oldCap = PlayerCapability[oldPlayer]
        val newCap = PlayerCapability[newPlayer]
        if (oldCap != null && newCap != null) {
            newCap.copyFrom(oldCap)
        }
        CapabilityLifecycle.invalidate(oldPlayer)
    }

    @JvmStatic
    fun onClientPlayerQuit(player: LocalPlayer?) {
        PlayerCapabilityClientStore.clear()
    }
}
