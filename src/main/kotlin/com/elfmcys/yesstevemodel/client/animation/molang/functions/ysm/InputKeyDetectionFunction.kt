package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm

import com.elfmcys.yesstevemodel.client.input.InputStateKey
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import com.elfmcys.yesstevemodel.molang.runtime.Function.ArgumentCollection
import com.elfmcys.yesstevemodel.util.InputUtil

object InputKeyDetectionFunction {
    class Keyboard : Function {
        override fun evaluate(context: ExecutionContext<*>, arguments: ArgumentCollection): Any? {
            if (!InputUtil.isPlayerReady()) {
                return false
            }
            for (i in 0 until arguments.size()) {
                val keycode: Int = arguments.getAsInt(context, i)
                if (keycode in 32..348 && InputStateKey.keyStates[keycode]) {
                    return true
                }
            }
            return false
        }

        override fun validateArgumentSize(size: Int): Boolean {
            return size >= 1
        }
    }

    class Mouse : Function {
        override fun evaluate(context: ExecutionContext<*>, arguments: ArgumentCollection): Any? {
            if (!InputUtil.isPlayerReady()) {
                return false
            }
            val keycode: Int = arguments.getAsInt(context, 0)
            if (keycode in 0..7) {
                return InputStateKey.mouseStates[keycode]
            }
            return false
        }

        override fun validateArgumentSize(size: Int): Boolean {
            return size == 1
        }
    }
}