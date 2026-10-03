package com.elfmcys.yesstevemodel.geckolib3.util

object MathHelper {
    /**
     * 将角度减小到 -180 到 +180 之间的角度，并进行 360 度检查
     */
    @JvmStatic
    fun wrapDegrees(value: Float): Float {
        var v = value % 360.0f
        if (v >= 180.0f) {
            v -= 360.0f
        }
        if (v < -180.0f) {
            v += 360.0f
        }
        return v
    }

    /**
     * 将角度减小到 -180 到 +180 之间的角度，并进行 360 度检查
     */
    @JvmStatic
    fun wrapDegrees(value: Double): Double {
        var v = value % 360.0
        if (v >= 180.0) {
            v -= 360.0
        }
        if (v < -180.0) {
            v += 360.0
        }
        return v
    }

    /**
     * 调整角度，使其值在 [-180, 180]
     */
    @JvmStatic
    fun wrapDegrees(angle: Int): Int {
        var a = angle % 360
        if (a >= 180) {
            a -= 360
        }
        if (a < -180) {
            a += 360
        }
        return a
    }
}