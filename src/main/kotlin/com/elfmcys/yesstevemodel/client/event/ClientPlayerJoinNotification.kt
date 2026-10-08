package com.elfmcys.yesstevemodel.client.event

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.client.ClientModelManager
import com.elfmcys.yesstevemodel.network.NetworkHandler
import kotlinx.coroutines.*
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.chat.Component
import kotlin.time.Duration.Companion.minutes

@Environment(EnvType.CLIENT)
object ClientPlayerJoinNotification {
    @JvmField
    var notified: Boolean = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var serverNotFoundJob: Job? = null

    init {
        ClientPlayConnectionEvents.JOIN.register { _, _, client ->
            client.player?.let(::onPlayerJoin)
        }
        ClientPlayConnectionEvents.DISCONNECT.register { _, client ->
            client.player?.let(::onPlayerQuit)
        }
    }

    @JvmStatic
    private fun onPlayerJoin(player: LocalPlayer) {
        if (notified) return
        ClientModelManager.runPendingModelCallback()
        notified = true
        if (!YesSteveModel.isAvailable) {
            YesSteveModel.sendUnavailableMessage()
            return
        }
        if (Minecraft.getInstance().isLocalServer) return
        serverNotFoundJob = scope.launch {
            runCatching {
                delay(1.minutes)
                Minecraft.getInstance().execute {
                    val localPlayer = Minecraft.getInstance().player
                    if (localPlayer != null && localPlayer.connection.isAcceptingMessages && !NetworkHandler.isConnectionValid(
                            localPlayer.connection.connection
                        )
                    ) {
                        localPlayer.displayClientMessage(
                            Component.translatable("message.yes_steve_model.client.server_not_found"),
                            false
                        )
                    }
                }
            }
        }
    }

    @JvmStatic
    private fun onPlayerQuit(player: LocalPlayer) {
        if (notified) {
            notified = false
            if (!YesSteveModel.isAvailable) {
                return
            }
            ClientModelManager.resetSync()
        }
        serverNotFoundJob?.cancel()
        serverNotFoundJob = null
    }
}
