package com.elfmcys.yesstevemodel.client.input

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.client.renderer.AnimationDebugOverlay
import com.elfmcys.yesstevemodel.util.InputUtil
import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.KeyMapping
import rip.ysm.api.client.KeyMappingFactory
import rip.ysm.api.client.event.ClientRawInputEvent
import rip.ysm.api.event.EventResult

@Environment(EnvType.CLIENT)
object DebugAnimationKey {
    val KEY_MAPPING: KeyMapping = KeyMappingFactory.createInGameAlt(
        "key.yes_steve_model.debug_animation.desc",
        InputConstants.Type.KEYSYM,
        66,
        KeyMappingFactory.YSM_CATEGORY
    )

    init {
        ClientRawInputEvent.KEY_PRESSED.register { _, action, event ->
            if (YesSteveModel.isAvailable && InputUtil.isPlayerReady() && action == 1 && InputUtil.isKeyPressed(
                    event,
                    KEY_MAPPING
                )
            ) {
                if (!AnimationDebugOverlay.isDebugActive) {
                    AnimationDebugOverlay.tryUpdateFromHitResult()
                } else {
                    AnimationDebugOverlay.clearActiveModel()
                }
            }
            EventResult.pass()
        }
    }
}