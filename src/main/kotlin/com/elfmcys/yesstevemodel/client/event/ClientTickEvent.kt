package com.elfmcys.yesstevemodel.client.event

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.audio.ObjectPool
import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.client.ClientModelManager
import com.elfmcys.yesstevemodel.client.upload.ModelUploadSession
import com.elfmcys.yesstevemodel.client.upload.UploadManager
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft

@Environment(EnvType.CLIENT)
object ClientTickEvent {
    @JvmField
    var tickCount: Int = 0

    @JvmField
    var refreshRate: Int = 60

    @JvmStatic
    fun register() {
        ClientTickEvents.START_CLIENT_TICK.register(::onClientPreTick)
    }

    @JvmStatic
    fun onClientPreTick(client: Minecraft) {
        if (!YesSteveModel.isAvailable()) {
            return
        }
        tickCount++
        UploadManager.processPendingUploads()
        ModelUploadSession.tickCurrent()
        ClientModelManager.flushPendingModels()
        ObjectPool.cleanup()
        refreshRate = client.window.refreshRate
        val localPlayer = client.player
        if (localPlayer != null) {
            PlayerCapability[localPlayer]?.tickAnimations()
        }
    }

    @JvmStatic
    fun getTickCount(): Int = tickCount

    @JvmStatic
    fun getRefreshRate(): Int = refreshRate
}
