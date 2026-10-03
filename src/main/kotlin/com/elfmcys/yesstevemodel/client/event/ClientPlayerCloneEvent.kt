package com.elfmcys.yesstevemodel.client.event

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.network.NetworkHandler
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.player.LocalPlayer
import rip.ysm.api.capability.CapabilityLifecycle

@Environment(EnvType.CLIENT)
object ClientPlayerCloneEvent {
    @JvmStatic
    fun onClientPlayerRespawn(oldPlayer: LocalPlayer, newPlayer: LocalPlayer) {
        if (!YesSteveModel.isAvailable() || !NetworkHandler.isClientConnected()) return
        CapabilityLifecycle.revive(oldPlayer)
        val oldCap = PlayerCapability[oldPlayer]
        val newCap = PlayerCapability[newPlayer]
        if (oldCap != null && newCap != null) {
            newCap.copyFrom(oldCap)
        }
        CapabilityLifecycle.invalidate(oldPlayer)
    }
}
