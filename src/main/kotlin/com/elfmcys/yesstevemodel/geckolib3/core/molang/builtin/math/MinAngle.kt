package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function

class MinAngle : Function {
    override fun evaluate(context: ExecutionContext<*>, arguments: Function.ArgumentCollection): Any {
        val angle: Float = arguments.getAsFloat(context, 0) % 360.0f
        return when {
            angle >= 180.0f -> angle - 360.0f
            angle < -180.0f -> angle + 360.0f
            else -> angle
        }
    }

    override fun validateArgumentSize(size: Int): Boolean {
        return size == 1
    }
}