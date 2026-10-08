package rip.ysm.gui.components

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import rip.ysm.gui.Option
import rip.ysm.gui.OptionRow
import java.text.DecimalFormat
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

open class SliderOptionRow(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    option: Option<Double>,
    min: Double,
    max: Double,
    step: Double,
    suffix: String?
) : OptionRow<Double>(x, y, width, height, option) {

    private val min: Double = min(min, max)
    private val max: Double = max(min, max)
    private val step: Double = max(step, 0.0)
    private val suffix: String = suffix ?: ""
    private val format: DecimalFormat = if (this.step >= 1.0) DecimalFormat("0") else DecimalFormat("0.0")
    private var dragging: Boolean = false

    override fun controlWidth(): Int = Mth.clamp(width / 2, 100, 260)

    override fun renderControl(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val cx = controlX()
        val cy = controlY()
        val cw = controlWidth()
        val ch = controlHeight()
        val hover = isMouseOverControl(mouseX.toDouble(), mouseY.toDouble()) || dragging

        g.fill(cx, cy, cx + cw, cy + ch, blendBg(hover, 0x41000000))
        g.renderOutline(cx, cy, cw, ch, 0x90FFFFFF.toInt())

        val value: Double = option?.get ?: this.min
        val range: Double = this.max - this.min
        val t: Double = if (range <= 0.0) 0.0 else Mth.clamp((value - this.min) / range, 0.0, 1.0)
        val fillW: Int = (t * (cw - 2)).toInt()
        g.fill(cx + 1, cy + 1, cx + 1 + fillW, cy + ch - 1, 0x50FFFFFF)

        val text: String = format.format(value) + suffix
        val tw: Int = Minecraft.getInstance().font.width(text)
        g.drawString(
            Minecraft.getInstance().font,
            Component.literal(text),
            cx + (cw - tw) / 2,
            cy + (ch - 8) / 2,
            -1,
            true
        )
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        if (isMouseOverControl(event.x(), event.y())) {
            dragging = true
            updateFromMouse(event.x())
        }
    }

    override fun onDrag(event: MouseButtonEvent, dx: Double, dy: Double) {
        if (dragging) {
            updateFromMouse(event.x())
        }
    }

    override fun onRelease(event: MouseButtonEvent) {
        dragging = false
    }

    private fun updateFromMouse(mouseX: Double) {
        val cx = controlX()
        val cw = controlWidth()
        val t: Double = Mth.clamp((mouseX - cx - 1) / (cw - 2), 0.0, 1.0)
        var raw: Double = min + t * (max - min)
        if (step > 0.0) {
            raw = step * (raw / step).roundToLong()
        }
        option?.setPending(Mth.clamp(raw, min, max))
    }
}