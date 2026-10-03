package com.elfmcys.yesstevemodel.client.event

import com.elfmcys.yesstevemodel.client.gui.DownloadScreen
import com.elfmcys.yesstevemodel.client.gui.PlayerModelScreen
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen

@Environment(EnvType.CLIENT)
object ModScreenEvent {
    const val IMC_METHOD: String = "DownloadScreen"

    @JvmField
    var receivedScreen: Screen? = null

    @JvmStatic
    fun setReceivedScreen(screen: Screen?) {
        receivedScreen = screen
    }

    @JvmStatic
    fun openScreen(modelScreen: PlayerModelScreen) {
        Minecraft.getInstance().setScreen(receivedScreen ?: DownloadScreen(modelScreen))
    }
}
