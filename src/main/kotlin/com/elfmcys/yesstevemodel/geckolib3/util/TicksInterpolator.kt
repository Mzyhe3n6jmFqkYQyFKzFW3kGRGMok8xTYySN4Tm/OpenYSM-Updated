package com.elfmcys.yesstevemodel.geckolib3.util

class TicksInterpolator(f: Float) : IInterpolable {
    private val tickDuration: Float = f * 20.0f

    override fun interpolate(f: Float): Float {
        if (tickDuration != 0.0f) {
            return f / tickDuration
        }
        return 1.0f
    }

    override fun getProgress(): Float {
        return tickDuration
    }
}