package com.elfmcys.yesstevemodel.event

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.model.ServerModelManager
import com.elfmcys.yesstevemodel.network.NetworkHandler
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents

object PlayerLogoutEvent {
    init {
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            if (!YesSteveModel.isAvailable()) {
                return@register
            }
            val player = handler.player ?: return@register
            if (NetworkHandler.isPlayerConnected(player)) {
                ServerModelManager.syncModelToPlayer(player.uuid)
            }
        }
    }
}
