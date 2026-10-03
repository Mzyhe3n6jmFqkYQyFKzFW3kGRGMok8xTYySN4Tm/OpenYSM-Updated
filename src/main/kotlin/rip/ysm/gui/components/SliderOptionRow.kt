package rip.ysm.gui.components

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import rip.ysm.gui.Option
import rip.ysm.gui.OptionRow
import java.text.DecimalFormat

open class SliderOptionRow : OptionRow<Double>() {
    var min: Double = 0.0
    var max: Double = 0.0
    var step: Double = 0.0
    var suffix: String = null
    var format: DecimalFormat = null
    var dragging: Boolean = false
    constructor(x: Int, y: Int, width: Int, height: Int, option: Option<Double>, min: Double, max: Double, step: Double, suffix: String) {
        super(x, y, width, height, option)
        this.min = Math.min(min, max)
        this.max = Math.max(min, max)
        this.step = Math.max(step, 0.0)
        this.suffix = if (suffix == null) "" else suffix
        this.format = if (this.step >= 1.0) DecimalFormat("0") else DecimalFormat("0.0")
    }
    open fun controlWidth(): Int {
        return Mth.clamp(width / 2, 100, 260)
    }
    open fun renderControl(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        var cx: Int = controlX()
        var cy: Int = controlY()
        var cw: Int = controlWidth()
        var ch: Int = controlHeight()
        var hover: Boolean = isMouseOverControl(mouseX, mouseY) || dragging
        g.fill(cx, cy, cx + cw, cy + ch, blendBg(hover, 0x41000000))
        g.renderOutline(cx, cy, cw, ch, (0x90FFFFFF).toInt())
        var value: Double = if (option.get() == null) min else option.get()
        var range: Double = max - min
        var t: Double = if (range <= 0.0) 0.0 else Mth.clamp(value - min / range, 0.0, 1.0)
        var fillW: Int = (t * cw - 2 as Int)
        g.fill(cx + 1, cy + 1, cx + 1 + fillW, cy + ch - 1, 0x50FFFFFF)
        var text: String = format.format(value) + suffix
        var tw: Int = Minecraft.getInstance().font.width(text)
        g.drawString(Minecraft.getInstance().font, Component.literal(text), cx + cw - tw / 2, cy + ch - 8 / 2, (0xFFFFFFFF).toInt(), true)
    }
    open fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        if (isMouseOverControl(event.x(), event.y())) {
            dragging = true
            updateFromMouse(event.x())
        }
    }
    open fun onDrag(event: MouseButtonEvent, dx: Double, dy: Double) {
        if (dragging) {
            updateFromMouse(event.x())
        }
    }
    open fun onRelease(event: MouseButtonEvent) {
        dragging = false
    }
    open fun updateFromMouse(mouseX: Double) {
        var cx: Int = controlX()
        var cw: Int = controlWidth()
        var t: Double = Mth.clamp(mouseX - cx - 1 / cw - 2, 0.0, 1.0)
        var raw: Double = min + t * max - min
        if (step > 0.0) {
            raw = step * Math.round(raw / step)
        }
        option.setPending(Mth.clamp(raw, min, max))
    }
}