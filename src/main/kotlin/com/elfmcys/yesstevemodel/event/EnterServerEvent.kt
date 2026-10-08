package com.elfmcys.yesstevemodel.event

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.capability.AuthModelsCapability
import com.elfmcys.yesstevemodel.capability.ModelInfoCapability
import com.elfmcys.yesstevemodel.capability.StarModelsCapability
import com.elfmcys.yesstevemodel.model.ServerModelManager
import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.network.message.S2CSyncAuthModelsPacket
import com.elfmcys.yesstevemodel.network.message.S2CSyncStarModelsPacket
import com.elfmcys.yesstevemodel.network.message.S2CVersionCheckPacket
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents

object EnterServerEvent {
    init {
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            if (!YesSteveModel.isAvailable) {
                return@register
            }
            val player = handler.player
            NetworkHandler.sendToClientPlayer(S2CVersionCheckPacket(), player)
            ModelInfoCapability[player]?.let { modelInfoCap ->
                if (!NetworkHandler.isPlayerConnected(player) && !modelInfoCap.isMandatory) {
                    modelInfoCap.markDirty()
                    return@let
                }
                modelInfoCap.stopAnimation(player)
                val syncMessage = modelInfoCap.createSyncMessage(player, false)
                if (syncMessage != null) {
                    NetworkHandler.sendToClientPlayer(syncMessage, player)
                } else {
                    modelInfoCap.markDirty()
                }
            }
            AuthModelsCapability[player]?.let { authModelsCap ->
                for (modelId in ServerModelManager.authModels) {
                    authModelsCap.addModel(modelId)
                }
                NetworkHandler.sendToClientPlayer(S2CSyncAuthModelsPacket(authModelsCap.authModels), player)
            }
            StarModelsCapability[player]?.let { starModelsCap ->
                NetworkHandler.sendToClientPlayer(S2CSyncStarModelsPacket(starModelsCap.starModels), player)
            }
        }
    }
}
