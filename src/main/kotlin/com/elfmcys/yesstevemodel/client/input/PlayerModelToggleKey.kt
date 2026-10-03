package com.elfmcys.yesstevemodel.client.input

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.client.gui.DisclaimerScreen
import com.elfmcys.yesstevemodel.client.gui.ExtraPlayerConfigScreen
import com.elfmcys.yesstevemodel.client.gui.PlayerModelScreen
import com.elfmcys.yesstevemodel.config.GeneralConfig
import com.elfmcys.yesstevemodel.config.ServerConfig
import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.util.InputUtil
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.input.KeyEvent
import rip.ysm.api.PlatformAPI
import rip.ysm.api.client.KeyMappingFactory
import rip.ysm.api.client.event.ClientRawInputEvent
import rip.ysm.api.event.EventResult

object PlayerModelToggleKey {
    @JvmField
    val KEY_MAPPING: KeyMapping = KeyMappingFactory.createInGameAlt(
        "key.yes_steve_model.player_model.desc",
        InputConstants.Type.KEYSYM,
        89,
        KeyMappingFactory.YSM_CATEGORY
    )

    @JvmStatic
    fun register() {
        if (PlatformAPI.isServer()) {
            return
        }
        ClientRawInputEvent.KEY_PRESSED.register { _, action, event ->
            onKeyInput(action, event)
            EventResult.pass()
        }
    }

    @JvmStatic
    private fun onKeyInput(action: Int, event: KeyEvent) {
        if (InputUtil.isPlayerReady() && action == 1 && InputUtil.isKeyPressed(event, KEY_MAPPING)) {
            if (!YesSteveModel.isAvailable()) {
                YesSteveModel.sendUnavailableMessage()
                return
            }
            val mc = Minecraft.getInstance()
            if (NetworkHandler.isClientConnected() && !ServerConfig.CAN_SWITCH_MODEL.get()) {
                mc.setScreen(ExtraPlayerConfigScreen(null))
            } else if (GeneralConfig.DISCLAIMER_SHOW.get()) {
                mc.setScreen(DisclaimerScreen())
            } else {
                mc.setScreen(PlayerModelScreen())
            }
        }
    }
}