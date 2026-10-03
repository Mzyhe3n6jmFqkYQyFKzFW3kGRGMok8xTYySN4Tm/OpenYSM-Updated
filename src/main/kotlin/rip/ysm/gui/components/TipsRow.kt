package rip.ysm.gui.components

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.util.FormattedCharSequence
import rip.ysm.gui.OptionRow
import java.util.List

class TipsRow : OptionRow<Any>() {
    var text: String = null
    var cachedLines: MutableList<FormattedCharSequence> = null
    var cachedWidth: Int = -1
    constructor(text: String) {
        super(0, 0, 0, 0, null)
        this.text = text
    }
    open fun recomputeLines() {
        if (cachedWidth == width && cachedLines != null) {
            return
        }
        var font: Font = Minecraft.getInstance().font
        cachedLines = font.split(Component.literal(text), Math.max(20, width - 16))
        cachedWidth = width
        this.height = Math.max(18, cachedLines.size() * 10 + 8)
    }
    open fun setWidth(w: Int) {
        super.setWidth(w)
        cachedLines = null
        cachedWidth = -1
        recomputeLines()
    }
    open fun renderWidget(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        recomputeLines()
        g.fill(getX(), getY(), getX() + width, getY() + height, (0x90000000).toInt())
        var font: Font = Minecraft.getInstance().font
        var y: Int = getY() + 4
        for (line in cachedLines) {
            g.drawString(font, line, getX() + 8, y, (0xFFEEEEEE).toInt(), false)
            y += 10
        }
    }
    open fun renderControl(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float)
}