package com.elfmcys.yesstevemodel.client.event

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.client.ClientModelManager
import com.elfmcys.yesstevemodel.client.ClientOnlyMode
import com.elfmcys.yesstevemodel.config.GeneralConfig
import com.elfmcys.yesstevemodel.network.NetworkHandler
import kotlinx.coroutines.*
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.chat.Component
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.minutes

@Environment(EnvType.CLIENT)
object ClientPlayerJoinNotification {
    private const val HANDSHAKE_PROBE_COUNT = 3

    var notified: Boolean = false

    private var handshakeProbeIndex = -1
    private var handshakeProbeDelay = 0
    private var handshakeElapsed = 0
    private var sessionId = 0

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var serverNotFoundJob: Job? = null

    private fun handshakeTimeoutTicks(): Int {
        val timeout = runCatching { GeneralConfig.HANDSHAKE_TIMEOUT.get() }.getOrDefault(5.0)
        return max(HANDSHAKE_PROBE_COUNT, (timeout * 20.0).roundToInt())
    }

    private fun probeInterval(): Int = max(1, handshakeTimeoutTicks() / (HANDSHAKE_PROBE_COUNT + 1))

    init {
        ClientPlayConnectionEvents.JOIN.register { _, _, client ->
            client.player?.let(::onPlayerJoin)
        }
        ClientPlayConnectionEvents.DISCONNECT.register { _, client ->
            client.player?.let(::onPlayerQuit)
        }
        ClientTickEvents.START_CLIENT_TICK.register(::onClientTick)
    }

    private fun onPlayerJoin(player: LocalPlayer) {
        if (notified) return
        ClientModelManager.runPendingModelCallback()
        notified = true
        if (!YesSteveModel.isAvailable) {
            YesSteveModel.sendUnavailableMessage()
            return
        }
        if (Minecraft.getInstance().isLocalServer) return
        if (ClientOnlyMode.isForced) {
            ClientOnlyMode.activateStandalone()
            return
        }
        handshakeProbeIndex = 0
        handshakeProbeDelay = 0
        handshakeElapsed = 0
        val currentSession = ++sessionId
        serverNotFoundJob = scope.launch {
            runCatching {
                delay(1.minutes)
                Minecraft.getInstance().execute {
                    if (currentSession != sessionId || ClientOnlyMode.isActive) return@execute
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

    private fun onClientTick(client: Minecraft) {
        ClientModelManager.applyClientOnlySelection()
        if (handshakeProbeIndex < 0 || client.isLocalServer) return
        val player = client.player ?: return
        if (!player.connection.isAcceptingMessages) return
        if (NetworkHandler.isConnectionValid(player.connection.connection)) {
            handshakeProbeIndex = -1
            return
        }
        handshakeElapsed++
        if (handshakeElapsed >= handshakeTimeoutTicks()) {
            handshakeProbeIndex = -1
            ClientOnlyMode.activateStandalone()
            return
        }
        if (handshakeProbeIndex >= HANDSHAKE_PROBE_COUNT) return
        if (handshakeProbeDelay > 0) {
            handshakeProbeDelay--
            return
        }
        NetworkHandler.sendVersionCheck(player.connection.connection)
        handshakeProbeIndex++
        handshakeProbeDelay = probeInterval()
    }

    private fun onPlayerQuit(player: LocalPlayer) {
        handshakeProbeIndex = -1
        sessionId++
        if (notified) {
            notified = false
            if (!YesSteveModel.isAvailable) return
            ClientModelManager.resetSync()
        }
        serverNotFoundJob?.cancel()
        serverNotFoundJob = null
    }
}
