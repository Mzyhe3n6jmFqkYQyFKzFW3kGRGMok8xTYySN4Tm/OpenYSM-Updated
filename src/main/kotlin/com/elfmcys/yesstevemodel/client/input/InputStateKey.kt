package com.elfmcys.yesstevemodel.client.input

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.util.InputUtil
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import rip.ysm.api.client.event.ClientRawInputEvent
import rip.ysm.api.event.EventResult

@Environment(EnvType.CLIENT)
object InputStateKey {
    @JvmField
    @Volatile
    var keyStates: BooleanArray = BooleanArray(349)

    @JvmField
    @Volatile
    var mouseStates: BooleanArray = BooleanArray(8)

    init {
        ClientRawInputEvent.KEY_PRESSED.register { _, action, event ->
            onKeyInput(event.key(), action)
            EventResult.pass()
        }
        ClientRawInputEvent.MOUSE_CLICKED_PRE.register { _, buttonInfo, action ->
            onMouseInput(buttonInfo.button(), action)
            EventResult.pass()
        }
    }

    private fun onKeyInput(keyCode: Int, action: Int) {
        if (YesSteveModel.isAvailable() && InputUtil.isPlayerReady() && keyCode in 32..348) {
            when (action) {
                1 -> {
                    keyStates[keyCode] = true
                }

                0 -> {
                    keyStates[keyCode] = false
                }
            }
        }
    }

    private fun onMouseInput(button: Int, action: Int) {
        if (YesSteveModel.isAvailable() && InputUtil.isPlayerReady() && button in 0..7) {
            when (action) {
                1 -> {
                    mouseStates[button] = true
                }

                0 -> {
                    mouseStates[button] = false
                }
            }
        }
    }
}