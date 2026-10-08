package com.elfmcys.yesstevemodel.geckolib3.core.util

import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

@Suppress("unused")
class Color(private val color: Int) {
    fun getColor(): Int = color
    fun getAlpha(): Int = color shr 24 and 0xFF
    fun getRed(): Int = color shr 16 and 0xFF
    fun getGreen(): Int = color shr 8 and 0xFF
    fun getBlue(): Int = color and 0xFF

    /**
     * 返回更加明亮的颜色
     *
     * @param factor 数值越高，颜色越亮
     * @return 更加明亮的颜色
     */
    fun brighter(factor: Double): Color {
        var r: Int = getRed()
        var g: Int = getGreen()
        var b: Int = getBlue()
        val i: Int = (1.0 / (1.0 - 1.0 / factor)).toInt()
        if (r == 0 && g == 0 && b == 0) {
            return ofRGBA(i, i, i, getAlpha())
        }
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
            getAlpha()
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
            max((getRed() * scale).toInt(), 0),
            max((getGreen() * scale).toInt(), 0),
            max((getBlue() * scale).toInt(), 0),
            getAlpha()
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
        @JvmField
        val WHITE: Color = Color(0xFFFFFFFF.toInt())

        @JvmField
        val LIGHT_GRAY: Color = Color(0xFFC0C0C0.toInt())

        @JvmField
        val GRAY: Color = Color(0xFF808080.toInt())

        @JvmField
        val DARK_GRAY: Color = Color(0x404040)

        @JvmField
        val BLACK: Color = Color(0xFF000000.toInt())

        @JvmField
        val RED: Color = Color(0xFFFF0000.toInt())

        @JvmField
        val PINK: Color = Color(0xFFFFAFAF.toInt())

        @JvmField
        val ORANGE: Color = Color(0xFFFFC800.toInt())

        @JvmField
        val YELLOW: Color = Color(0xFFFFFF00.toInt())

        @JvmField
        val GREEN: Color = Color(0x00FF00)

        @JvmField
        val MAGENTA: Color = Color(0xFFFF00FF.toInt())

        @JvmField
        val CYAN: Color = Color(0x00FFFF)

        @JvmField
        val BLUE: Color = Color(0x0000FF)

        @JvmStatic
        fun ofTransparent(color: Int): Color = Color(color)

        @JvmStatic
        fun ofOpaque(color: Int): Color = Color(0xFF000000.toInt() or color)

        @JvmStatic
        fun ofRGB(r: Float, g: Float, b: Float): Color = ofRGBA(r, g, b, 1.0f)

        @JvmStatic
        fun ofRGB(r: Int, g: Int, b: Int): Color = ofRGBA(r, g, b, 255)

        @JvmStatic
        fun ofRGBA(r: Float, g: Float, b: Float, a: Float): Color {
            return ofRGBA(
                (r * 255.0f + 0.5f).toInt(),
                (g * 255.0f + 0.5f).toInt(),
                (b * 255.0f + 0.5f).toInt(),
                (a * 255.0f + 0.5f).toInt()
            )
        }

        @JvmStatic
        fun ofRGBA(r: Int, g: Int, b: Int, a: Int): Color {
            return Color(a and 0xFF shl 24 or (r and 0xFF shl 16) or (g and 0xFF shl 8) or (b and 0xFF))
        }

        @JvmStatic
        fun ofHSB(hue: Float, saturation: Float, brightness: Float): Color {
            return ofOpaque(HSBtoRGB(hue, saturation, brightness))
        }

        @JvmStatic
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