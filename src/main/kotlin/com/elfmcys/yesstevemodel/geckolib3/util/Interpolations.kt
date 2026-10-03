package com.elfmcys.yesstevemodel.geckolib3.util

import kotlin.math.abs
import kotlin.math.withSign

object Interpolations {
    /**
     * 线性插值
     */
    @JvmStatic
    fun lerp(a: Float, b: Float, position: Float): Float {
        return a + (b - a) * position
    }

    /**
     * 用于插值 yaw 的特殊插值方法
     */
    @JvmStatic
    fun lerpYaw(a: Float, b: Float, position: Float): Float {
        val wrappedA = MathHelper.wrapDegrees(a)
        val wrappedB = MathHelper.wrapDegrees(b)
        return lerp(wrappedA, normalizeYaw(wrappedA, wrappedB), position)
    }

    /**
     * 在 y1 和 y2 之间使用 Hermite 三次插值
     */
    @JvmStatic
    fun cubicHermite(y0: Double, y1: Double, y2: Double, y3: Double, x: Double): Double {
        val a = -0.5 * y0 + 1.5 * y1 - 1.5 * y2 + 0.5 * y3
        val b = y0 - 2.5 * y1 + 2.0 * y2 - 0.5 * y3
        val c = -0.5 * y0 + 0.5 * y2
        return ((a * x + b) * x + c) * x + y1
    }

    /**
     * Yaw 的 Hermite 三次插值
     */
    @JvmStatic
    fun cubicHermiteYaw(y0: Float, y1: Float, y2: Float, y3: Float, position: Float): Double {
        val wrappedY0 = MathHelper.wrapDegrees(y0)
        var wrappedY1 = MathHelper.wrapDegrees(y1)
        var wrappedY2 = MathHelper.wrapDegrees(y2)
        var wrappedY3 = MathHelper.wrapDegrees(y3)
        wrappedY1 = normalizeYaw(wrappedY0, wrappedY1)
        wrappedY2 = normalizeYaw(wrappedY1, wrappedY2)
        wrappedY3 = normalizeYaw(wrappedY2, wrappedY3)
        return cubicHermite(wrappedY0.toDouble(), wrappedY1.toDouble(), wrappedY2.toDouble(), wrappedY3.toDouble(), position.toDouble())
    }

    /**
     * Yaw 的 Hermite 三次插值
     */
    @JvmStatic
    fun cubicHermiteYaw(y0: Double, y1: Double, y2: Double, y3: Double, position: Double): Double {
        val wrappedY0 = MathHelper.wrapDegrees(y0)
        var wrappedY1 = MathHelper.wrapDegrees(y1)
        var wrappedY2 = MathHelper.wrapDegrees(y2)
        var wrappedY3 = MathHelper.wrapDegrees(y3)
        wrappedY1 = normalizeYaw(wrappedY0, wrappedY1)
        wrappedY2 = normalizeYaw(wrappedY1, wrappedY2)
        wrappedY3 = normalizeYaw(wrappedY2, wrappedY3)
        return cubicHermite(wrappedY0, wrappedY1, wrappedY2, wrappedY3, position)
    }

    /**
     * y1 和 y2 之间的三次插值
     */
    @JvmStatic
    fun cubic(y0: Float, y1: Float, y2: Float, y3: Float, x: Float): Float {
        val a = y3 - y2 - y0 + y1
        val b = y0 - y1 - a
        val c = y2 - y0
        return ((a * x + b) * x + c) * x + y1
    }

    /**
     * Yaw 的三次插值
     */
    @JvmStatic
    fun cubicYaw(y0: Float, y1: Float, y2: Float, y3: Float, position: Float): Float {
        val wrappedY0 = MathHelper.wrapDegrees(y0)
        var wrappedY1 = MathHelper.wrapDegrees(y1)
        var wrappedY2 = MathHelper.wrapDegrees(y2)
        var wrappedY3 = MathHelper.wrapDegrees(y3)
        wrappedY1 = normalizeYaw(wrappedY0, wrappedY1)
        wrappedY2 = normalizeYaw(wrappedY1, wrappedY2)
        wrappedY3 = normalizeYaw(wrappedY2, wrappedY3)
        return cubic(wrappedY0, wrappedY1, wrappedY2, wrappedY3, position)
    }

    @JvmStatic
    fun bezierX(x1: Float, x2: Float, t: Float, epsilon: Float): Float {
        var x = t
        var init = bezier(0.0f, x1, x2, 1.0f, t)
        var factor = 0.1f.withSign(t - init)
        while (abs(t - init) > epsilon) {
            val oldFactor = factor
            x += factor
            init = bezier(0.0f, x1, x2, 1.0f, x)
            if (factor.withSign(t - init) != oldFactor) {
                factor *= -0.25f
            }
        }
        return x
    }

    @JvmStatic
    fun bezierX(x1: Float, x2: Float, t: Float): Float {
        return bezierX(x1, x2, t, 0.0005f)
    }

    @JvmStatic
    fun bezier(x1: Float, x2: Float, x3: Float, x4: Float, t: Float): Float {
        val t1 = lerp(x1, x2, t)
        val t2 = lerp(x2, x3, t)
        val t3 = lerp(x3, x4, t)
        val t4 = lerp(t1, t2, t)
        val t5 = lerp(t2, t3, t)
        return lerp(t4, t5, t)
    }

    @JvmStatic
    fun normalizeYaw(a: Float, b: Float): Float {
        val diff = a - b
        if (diff > 180.0f || diff < -180.0f) {
            val normalizedDiff = (360.0f - abs(diff)).withSign(diff)
            return a + normalizedDiff
        }
        return b
    }

    @JvmStatic
    fun envelope(x: Float, duration: Float, fades: Float): Float {
        return envelope(x, 0.0f, fades, duration - fades, duration)
    }

    @JvmStatic
    fun envelope(x: Float, lowIn: Float, lowOut: Float, highIn: Float, highOut: Float): Float {
        if (x < lowIn || x > highOut) {
            return 0.0f
        }
        if (x < lowOut) {
            return (x - lowIn) / (lowOut - lowIn)
        }
        if (x > highIn) {
            return 1.0f - (x - highIn) / (highOut - highIn)
        }
        return 1.0f
    }

    /* --- double 版本的函数 --- */

    @JvmStatic
    fun lerp(a: Double, b: Double, position: Double): Double {
        return a + (b - a) * position
    }

    @JvmStatic
    fun lerpYaw(a: Double, b: Double, position: Double): Double {
        val wrappedA = MathHelper.wrapDegrees(a)
        val wrappedB = MathHelper.wrapDegrees(b)
        return lerp(wrappedA, normalizeYaw(wrappedA, wrappedB), position)
    }

    @JvmStatic
    fun cubic(y0: Double, y1: Double, y2: Double, y3: Double, x: Double): Double {
        val a = y3 - y2 - y0 + y1
        val b = y0 - y1 - a
        val c = y2 - y0
        return ((a * x + b) * x + c) * x + y1
    }

    @JvmStatic
    fun cubicYaw(y0: Double, y1: Double, y2: Double, y3: Double, position: Double): Double {
        val wrappedY0 = MathHelper.wrapDegrees(y0)
        var wrappedY1 = MathHelper.wrapDegrees(y1)
        var wrappedY2 = MathHelper.wrapDegrees(y2)
        var wrappedY3 = MathHelper.wrapDegrees(y3)
        wrappedY1 = normalizeYaw(wrappedY0, wrappedY1)
        wrappedY2 = normalizeYaw(wrappedY1, wrappedY2)
        wrappedY3 = normalizeYaw(wrappedY2, wrappedY3)
        return cubic(wrappedY0, wrappedY1, wrappedY2, wrappedY3, position)
    }

    @JvmStatic
    fun bezierX(x1: Double, x2: Double, t: Double, epsilon: Double): Double {
        var x = t
        var init = bezier(0.0, x1, x2, 1.0, t)
        var factor = 0.1.withSign(t - init)
        while (abs(t - init) > epsilon) {
            val oldFactor = factor
            x += factor
            init = bezier(0.0, x1, x2, 1.0, x)
            if (factor.withSign(t - init) != oldFactor) {
                factor *= -0.25
            }
        }
        return x
    }

    @JvmStatic
    fun bezierX(x1: Double, x2: Double, t: Float): Double {
        return bezierX(x1, x2, t.toDouble(), 0.0005)
    }

    @JvmStatic
    fun bezier(x1: Double, x2: Double, x3: Double, x4: Double, t: Double): Double {
        val t1 = lerp(x1, x2, t)
        val t2 = lerp(x2, x3, t)
        val t3 = lerp(x3, x4, t)
        val t4 = lerp(t1, t2, t)
        val t5 = lerp(t2, t3, t)
        return lerp(t4, t5, t)
    }

    @JvmStatic
    fun normalizeYaw(a: Double, b: Double): Double {
        val diff = a - b
        if (diff > 180.0 || diff < -180.0) {
            val normalizedDiff = (360.0 - abs(diff)).withSign(diff)
            return a + normalizedDiff
        }
        return b
    }

    @JvmStatic
    fun envelope(x: Double, duration: Double, fades: Double): Double {
        return envelope(x, 0.0, fades, duration - fades, duration)
    }

    @JvmStatic
    fun envelope(x: Double, lowIn: Double, lowOut: Double, highIn: Double, highOut: Double): Double {
        if (x < lowIn || x > highOut) {
            return 0.0
        }
        if (x < lowOut) {
            return (x - lowIn) / (lowOut - lowIn)
        }
        if (x > highIn) {
            return 1.0 - (x - highIn) / (highOut - highIn)
        }
        return 1.0
    }
}