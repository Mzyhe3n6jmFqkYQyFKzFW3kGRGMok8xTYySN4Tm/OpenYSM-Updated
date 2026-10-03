package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function

open class MinAngle : Function {
    open fun evaluate(context: ExecutionContext<*>, arguments: ArgumentCollection): Any {
        var angle: Float = arguments.getAsFloat(context, 0) % 360.0f
        if (angle >= 180.0f) {
            return angle - 360.0f
        } else {
            if (angle < -180.0f) {
                return angle + 360.0f
            } else {
                return angle
            }
        }
    }
    open fun validateArgumentSize(size: Int): Boolean {
        return size == 1
    }
}