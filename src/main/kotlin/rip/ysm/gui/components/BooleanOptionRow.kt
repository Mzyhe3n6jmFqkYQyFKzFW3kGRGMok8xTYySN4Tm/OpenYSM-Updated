package rip.ysm.gui.components

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.MouseButtonEvent
import rip.ysm.gui.Option
import rip.ysm.gui.OptionRow
import kotlin.math.min

open class BooleanOptionRow(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    option: Option<Boolean>
) : OptionRow<Boolean>(x, y, width, height, option) {

    override fun renderControl(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val size = min(controlHeight(), 14)
        val cx = controlX() + controlWidth() - size
        val cy = controlY() + (controlHeight() - size) / 2
        val value = option?.value ?: false
        val hover = isMouseOverControl(mouseX.toDouble(), mouseY.toDouble())
        g.fill(cx, cy, cx + size, cy + size, blendBg(hover, 0xFF1A1A1A.toInt()))
        g.renderOutline(cx, cy, size, size, -1)
        if (value) {
            g.fill(cx + 3, cy + 3, cx + size - 3, cy + size - 3, -1)
        }
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        if (isMouseOverControl(event.x(), event.y())) {
            option?.let {
                it.value = !it.value
            }
        }
    }
}