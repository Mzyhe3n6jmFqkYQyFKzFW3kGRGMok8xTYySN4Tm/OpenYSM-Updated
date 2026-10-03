package com.elfmcys.yesstevemodel.client.input

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.util.InputUtil
import rip.ysm.api.PlatformAPI
import rip.ysm.api.client.event.ClientRawInputEvent
import rip.ysm.api.event.EventResult

object InputStateKey {
    @JvmField
    @Volatile
    var keyStates: BooleanArray = BooleanArray(349)

    @JvmField
    @Volatile
    var mouseStates: BooleanArray = BooleanArray(8)

    @JvmStatic
    fun register() {
        if (PlatformAPI.isServer()) {
            return
        }
        ClientRawInputEvent.KEY_PRESSED.register { _, action, event ->
            onKeyInput(event.key(), action)
            EventResult.pass()
        }
        ClientRawInputEvent.MOUSE_CLICKED_PRE.register { _, buttonInfo, action ->
            onMouseInput(buttonInfo.button(), action)
            EventResult.pass()
        }
    }

    @JvmStatic
    private fun onKeyInput(keyCode: Int, action: Int) {
        if (YesSteveModel.isAvailable() && InputUtil.isPlayerReady() && keyCode in 32..348) {
            if (action == 1) {
                keyStates[keyCode] = true
            } else if (action == 0) {
                keyStates[keyCode] = false
            }
        }
    }

    @JvmStatic
    private fun onMouseInput(button: Int, action: Int) {
        if (YesSteveModel.isAvailable() && InputUtil.isPlayerReady() && button in 0..7) {
            if (action == 1) {
                mouseStates[button] = true
            } else if (action == 0) {
                mouseStates[button] = false
            }
        }
    }
}