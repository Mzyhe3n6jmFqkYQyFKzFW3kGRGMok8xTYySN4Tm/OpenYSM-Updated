@file:Suppress("unused")

package com.elfmcys.yesstevemodel.geckolib3.core.util

import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class Color(val color: Int) {
    val alpha: Int
        get() = color shr 24 and 0xFF
    val red: Int
        get() = color shr 16 and 0xFF
    val green: Int
        get() = color shr 8 and 0xFF
    val blue: Int
        get() = color and 0xFF

    /**
     * 返回更加明亮的颜色
     *
     * @param factor 数值越高，颜色越亮
     * @return 更加明亮的颜色
     */
    fun brighter(factor: Double): Color {
        var r = red
        var g = green
        var b = blue
        val i = (1.0 / (1.0 - 1.0 / factor)).toInt()
        if (r == 0 && g == 0 && b == 0) return ofRGBA(i, i, i, alpha)
        if (r in 1..<i) {
            r = i
        }
        if (g in 1..<i) {
            g = i
        }
        if (b in 1..<i) {
            b = i
        }
        val scale = 1.0 / factor
        return ofRGBA(
            min((r / scale).toInt(), 255),
            min((g / scale).toInt(), 255),
            min((b / scale).toInt(), 255),
            alpha
        )
    }

    /**
     * 返回更加深暗的颜色
     *
     * @param factor 数值越高，颜色越暗
     * @return 更加深暗的颜色
     */
    fun darker(factor: Double): Color {
        val scale = 1.0 / factor
        return ofRGBA(
            max((red * scale).toInt(), 0),
            max((green * scale).toInt(), 0),
            max((blue * scale).toInt(), 0),
            alpha
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Color) return false
        return color == other.color
    }

    override fun hashCode(): Int = color

    override fun toString(): String = color.toString()

    companion object {
        val WHITE: Color = Color(0xFFFFFFFF.toInt())

        val LIGHT_GRAY: Color = Color(0xFFC0C0C0.toInt())

        val GRAY: Color = Color(0xFF808080.toInt())

        val DARK_GRAY: Color = Color(0x404040)

        val BLACK: Color = Color(0xFF000000.toInt())

        val RED: Color = Color(0xFFFF0000.toInt())

        val PINK: Color = Color(0xFFFFAFAF.toInt())

        val ORANGE: Color = Color(0xFFFFC800.toInt())

        val YELLOW: Color = Color(0xFFFFFF00.toInt())

        val GREEN: Color = Color(0x00FF00)

        val MAGENTA: Color = Color(0xFFFF00FF.toInt())

        val CYAN: Color = Color(0x00FFFF)

        val BLUE: Color = Color(0x0000FF)

        fun ofTransparent(color: Int): Color = Color(color)

        fun ofOpaque(color: Int): Color = Color(0xFF000000.toInt() or color)

        fun ofRGB(r: Float, g: Float, b: Float): Color = ofRGBA(r, g, b, 1.0f)

        fun ofRGB(r: Int, g: Int, b: Int): Color = ofRGBA(r, g, b, 255)

        fun ofRGBA(r: Float, g: Float, b: Float, a: Float): Color {
            return ofRGBA(
                (r * 255.0f + 0.5f).toInt(),
                (g * 255.0f + 0.5f).toInt(),
                (b * 255.0f + 0.5f).toInt(),
                (a * 255.0f + 0.5f).toInt()
            )
        }

        fun ofRGBA(r: Int, g: Int, b: Int, a: Int): Color {
            return Color(a and 0xFF shl 24 or (r and 0xFF shl 16) or (g and 0xFF shl 8) or (b and 0xFF))
        }

        fun ofHSB(hue: Float, saturation: Float, brightness: Float): Color {
            return ofOpaque(HSBtoRGB(hue, saturation, brightness))
        }

        fun HSBtoRGB(hue: Float, saturation: Float, brightness: Float): Int {
            var r = 0
            var g = 0
            var b = 0
            if (saturation == 0.0f) {
                val v = (brightness * 255.0f + 0.5f).toInt()
                r = v
                g = v
                b = v
            } else {
                val h = (hue - floor(hue)) * 6.0f
                val f = h - floor(h)
                val p = brightness * (1.0f - saturation)
                val q = brightness * (1.0f - saturation * f)
                val t = brightness * (1.0f - saturation * (1.0f - f))
                when (h.toInt()) {
                    0 -> {
                        r = (brightness * 255.0f + 0.5f).toInt()
                        g = (t * 255.0f + 0.5f).toInt()
                        b = (p * 255.0f + 0.5f).toInt()
                    }

                    1 -> {
                        r = (q * 255.0f + 0.5f).toInt()
                        g = (brightness * 255.0f + 0.5f).toInt()
                        b = (p * 255.0f + 0.5f).toInt()
                    }

                    2 -> {
                        r = (p * 255.0f + 0.5f).toInt()
                        g = (brightness * 255.0f + 0.5f).toInt()
                        b = (t * 255.0f + 0.5f).toInt()
                    }

                    3 -> {
                        r = (p * 255.0f + 0.5f).toInt()
                        g = (q * 255.0f + 0.5f).toInt()
                        b = (brightness * 255.0f + 0.5f).toInt()
                    }

                    4 -> {
                        r = (t * 255.0f + 0.5f).toInt()
                        g = (p * 255.0f + 0.5f).toInt()
                        b = (brightness * 255.0f + 0.5f).toInt()
                    }

                    5 -> {
                        r = (brightness * 255.0f + 0.5f).toInt()
                        g = (p * 255.0f + 0.5f).toInt()
                        b = (q * 255.0f + 0.5f).toInt()
                    }
                }
            }
            return 0xFF000000.toInt() or (r and 0xFF shl 16) or (g and 0xFF shl 8) or (b and 0xFF)
        }
    }
}