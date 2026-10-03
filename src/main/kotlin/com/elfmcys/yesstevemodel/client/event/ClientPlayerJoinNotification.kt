package com.elfmcys.yesstevemodel.client.event

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.client.ClientModelManager
import com.elfmcys.yesstevemodel.network.NetworkHandler
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.chat.Component
import kotlin.concurrent.thread

object ClientPlayerJoinNotification {
    @JvmField
    var notified: Boolean = false

    @JvmStatic
    fun register() {
        ClientPlayConnectionEvents.JOIN.register { _, _, client ->
            client.player?.let(::onPlayerJoin)
        }
        ClientPlayConnectionEvents.DISCONNECT.register { _, client ->
            client.player?.let(::onPlayerQuit)
        }
    }

    @JvmStatic
    fun onPlayerJoin(player: LocalPlayer) {
        if (notified) {
            return
        }
        ClientModelManager.runPendingModelCallback()
        notified = true
        if (!YesSteveModel.isAvailable()) {
            YesSteveModel.sendUnavailableMessage()
            return
        }
        if (Minecraft.getInstance().isLocalServer) {
            return
        }
        thread(isDaemon = true) {
            runCatching {
                Thread.sleep(60000L)
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
    fun onPlayerQuit(player: LocalPlayer) {
        if (notified) {
            notified = false
            if (!YesSteveModel.isAvailable()) {
                return
            }
            ClientModelManager.resetSync()
        }
    }
}
