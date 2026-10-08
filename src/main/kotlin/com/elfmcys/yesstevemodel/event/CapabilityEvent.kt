package com.elfmcys.yesstevemodel.event

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.capability.*
import com.elfmcys.yesstevemodel.config.ServerConfig
import com.elfmcys.yesstevemodel.model.ServerModelManager
import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.network.message.*
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.Projectile
import rip.ysm.api.capability.CapabilityLifecycle

object CapabilityEvent {
    init {
        ServerPlayerEvents.COPY_FROM.register { oldPlayer, newPlayer, alive ->
            onPlayerCloned(oldPlayer, newPlayer, !alive)
        }
        ServerEntityEvents.ENTITY_LOAD.register(::onEntityAdd)
        ServerTickEvents.END_SERVER_TICK.register(::onServerTick)
    }

    private fun onPlayerCloned(oldPlayer: ServerPlayer, newPlayer: ServerPlayer, wasDeath: Boolean) {
        if (!YesSteveModel.isAvailable()) return
        CapabilityLifecycle.revive(oldPlayer)
        val oldModelInfoCap = getModelInfoCap(oldPlayer)
        val oldAuthModelsCap = getAuthModelsCap(oldPlayer)
        val oldStarModelsCap = getStarModelsCap(oldPlayer)
        val modelInfoCap = getModelInfoCap(newPlayer)
        val authModelsCap = getAuthModelsCap(newPlayer)
        val starModelsCap = getStarModelsCap(newPlayer)
        if (modelInfoCap != null && oldModelInfoCap != null) modelInfoCap.copyFrom(oldModelInfoCap)
        if (authModelsCap != null && oldAuthModelsCap != null) authModelsCap.authModels = oldAuthModelsCap.authModels
        if (starModelsCap != null && oldStarModelsCap != null) starModelsCap.starModels = oldStarModelsCap.starModels
        CapabilityLifecycle.invalidate(oldPlayer)
    }

    private fun onEntityAdd(entity: Entity, level: ServerLevel) {
        if (!YesSteveModel.isAvailable()) return
        if (entity is ServerPlayer) {
            getModelInfoCap(entity)?.let { modelInfoCap ->
                if (!NetworkHandler.isPlayerConnected(entity) && !modelInfoCap.isMandatory) {
                    modelInfoCap.markDirty()
                    return@let
                }
                modelInfoCap.stopAnimation(entity)
                val syncMessage = modelInfoCap.createSyncMessage(entity, false)
                if (syncMessage != null) {
                    NetworkHandler.sendToClientPlayer(syncMessage, entity)
                } else {
                    modelInfoCap.markDirty()
                }
            }
            getAuthModelsCap(entity)?.let { authModelsCap ->
                for (modelId in ServerModelManager.getAuthModels()) {
                    authModelsCap.addModel(modelId)
                }
                NetworkHandler.sendToClientPlayer(S2CSyncAuthModelsPacket(authModelsCap.authModels), entity)
            }
            getStarModelsCap(entity)?.let { starModelsCap ->
                NetworkHandler.sendToClientPlayer(S2CSyncStarModelsPacket(starModelsCap.starModels), entity)
            }
        }
    }

    private fun onServerTick(server: MinecraftServer) {
        if (!YesSteveModel.isAvailable()) return
        val players = server.playerList.players
        val lowBandwidth = ServerConfig.LOW_BANDWIDTH_USAGE.get()
        for (serverPlayer in players) {
            getModelInfoCap(serverPlayer)?.let { cap ->
                if (!NetworkHandler.isPlayerConnected(serverPlayer) && !cap.isMandatory) {
                    if (serverPlayer.tickCount == 200 || serverPlayer.tickCount == 600 || serverPlayer.tickCount == 1800) {
                        NetworkHandler.sendToClientPlayer(S2CVersionCheckPacket(), serverPlayer)
                    }
                    return@let
                }
                if (cap.isDirty) {
                    cap.animSync.updateAndSync(serverPlayer, false, lowBandwidth)
                    cap.createSyncMessage(serverPlayer, true)?.let { message ->
                        cap.clearDirty()
                        NetworkHandler.sendToTrackingEntityAndSelf(message, serverPlayer)
                        val vehicle = serverPlayer.vehicle
                        if (vehicle != null && vehicle.firstPassenger == serverPlayer) {
                            syncVehicleModel(vehicle, serverPlayer)
                        }
                    }
                } else {
                    cap.animSync.updateAndSync(serverPlayer, true, lowBandwidth)
                }
            }
        }
    }

    @JvmStatic
    fun syncProjectileModel(projectile: Projectile, serverPlayer: ServerPlayer) {
        ModelInfoCapability[serverPlayer]?.let { modelInfoCap ->
            if (!NetworkHandler.isPlayerConnected(serverPlayer) && !modelInfoCap.isMandatory) {
                return
            }
            ProjectileModelCapability[projectile]?.let { projectileModelCap ->
                modelInfoCap.withMolangVars { vars ->
                    projectileModelCap.setModel(modelInfoCap.modelId, vars)
                    NetworkHandler.sendToTrackingEntity(
                        S2CSyncProjectileModelPacket(projectile.id, projectileModelCap),
                        projectile
                    )
                }
            }
        }
    }

    @JvmStatic
    fun syncVehicleModel(entity: Entity, serverPlayer: ServerPlayer) {
        ModelInfoCapability[serverPlayer]?.let { modelInfoCap ->
            if (!NetworkHandler.isPlayerConnected(serverPlayer) && !modelInfoCap.isMandatory) {
                return
            }
            VehicleModelCapability[entity]?.let { vehicleModelCap ->
                modelInfoCap.molangVars?.let { vars ->
                    vehicleModelCap.setModel(modelInfoCap.modelId, vars)
                    NetworkHandler.sendToTrackingEntity(S2CSyncVehicleModelPacket(entity.id, vehicleModelCap), entity)
                }
            }
        }
    }

    @JvmStatic
    fun getModelInfoCap(player: Player): ModelInfoCapability? = ModelInfoCapability[player]

    @JvmStatic
    fun getAuthModelsCap(player: Player): AuthModelsCapability? = AuthModelsCapability[player]

    @JvmStatic
    fun getStarModelsCap(player: Player): StarModelsCapability? = StarModelsCapability[player]
}
