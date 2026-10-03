package rip.ysm.gui.components

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.MouseButtonEvent
import rip.ysm.gui.Option
import rip.ysm.gui.OptionRow

open class BooleanOptionRow : OptionRow<Boolean>() {
    constructor(x: Int, y: Int, width: Int, height: Int, option: Option<Boolean>) {
        super(x, y, width, height, option)
    }
    open fun renderControl(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        var size: Int = Math.min(controlHeight(), 14)
        var cx: Int = controlX() + controlWidth() - size
        var cy: Int = controlY() + controlHeight() - size / 2
        var value: Boolean = option.get()
        var hover: Boolean = isMouseOverControl(mouseX, mouseY)
        g.fill(cx, cy, cx + size, cy + size, blendBg(hover, (0xFF1A1A1A).toInt()))
        g.renderOutline(cx, cy, size, size, -1)
        if (value) {
            g.fill(cx + 3, cy + 3, cx + size - 3, cy + size - 3, -1)
        }
    }
    open fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        if (isMouseOverControl(event.x(), event.y())) {
            option.setPending(!option.get())
        }
    }
}