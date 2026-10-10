package com.elfmcys.yesstevemodel.event

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.capability.ModelInfoCapability
import com.elfmcys.yesstevemodel.model.ServerModelManager
import com.elfmcys.yesstevemodel.model.ServerModelSelection
import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.network.message.S2CSyncAuthModelsPacket
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
                val savedModel = ServerModelSelection.getPlayerModel(player.uuid)
                val savedTexture = ServerModelSelection.getPlayerTexture(player.uuid)
                if (savedModel != null && ServerModelManager.serverModelInfo.containsKey(savedModel)) {
                    val modelData = ServerModelManager.serverModelInfo[savedModel]
                    val validTexture =
                        if (savedTexture != null && modelData?.modelInfo?.textures?.contains(savedTexture) == true) {
                            savedTexture
                        } else {
                            modelData?.modelInfo?.textures?.firstOrNull() ?: "default"
                        }
                    modelInfoCap.setModelAndTexture(savedModel, validTexture)
                } else if (modelInfoCap.modelId.isNotBlank()) {
                    ServerModelSelection.savePlayerSelection(
                        player.uuid,
                        modelInfoCap.modelId,
                        modelInfoCap.selectTexture
                    )
                }
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
            val savedAuth = ServerModelSelection.getAuthModels(player.uuid)
            NetworkHandler.sendToClientPlayer(S2CSyncAuthModelsPacket(savedAuth.toMutableSet()), player)
        }
    }
}
