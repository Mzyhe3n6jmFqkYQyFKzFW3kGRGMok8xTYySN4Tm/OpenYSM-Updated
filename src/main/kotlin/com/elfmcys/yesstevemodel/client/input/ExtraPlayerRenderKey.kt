package com.elfmcys.yesstevemodel.client.input

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.client.gui.ExtraPlayerRenderScreen
import com.elfmcys.yesstevemodel.util.InputUtil
import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import rip.ysm.api.client.KeyMappingFactory
import rip.ysm.api.client.event.ClientRawInputEvent
import rip.ysm.api.event.EventResult

@Environment(EnvType.CLIENT)
object ExtraPlayerRenderKey {
    @JvmField
    val KEY_MAPPING: KeyMapping = KeyMappingFactory.createInGameAlt(
        "key.yes_steve_model.open_extra_player_render.desc",
        InputConstants.Type.KEYSYM,
        80,
        KeyMappingFactory.YSM_CATEGORY
    )

    init {
        ClientRawInputEvent.KEY_PRESSED.register { _, action, event ->
            if (YesSteveModel.isAvailable() && InputUtil.isPlayerReady() && action == 1 && InputUtil.isKeyPressed(
                    event,
                    KEY_MAPPING
                )
            ) Minecraft.getInstance().setScreen(ExtraPlayerRenderScreen())
            EventResult.pass()
        }
    }
}