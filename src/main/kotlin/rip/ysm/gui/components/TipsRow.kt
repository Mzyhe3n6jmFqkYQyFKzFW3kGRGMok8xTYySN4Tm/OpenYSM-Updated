package rip.ysm.gui.components

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.util.FormattedCharSequence
import rip.ysm.gui.OptionRow
import kotlin.math.max

class TipsRow(val text: String) : OptionRow<Any?>(0, 0, 0, 0, null) {

    private var cachedLines: List<FormattedCharSequence>? = null
    private var cachedWidth: Int = -1

    private fun recomputeLines() {
        if (cachedWidth == width && cachedLines != null) {
            return
        }
        val font: Font = Minecraft.getInstance().font
        val lines = font.split(Component.literal(text), max(20, width - 16))
        cachedLines = lines
        cachedWidth = width
        this.height = max(18, lines.size * 10 + 8)
    }

    override fun setWidth(w: Int) {
        super.setWidth(w)
        cachedLines = null
        cachedWidth = -1
        recomputeLines()
    }

    override fun renderWidget(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        recomputeLines()
        g.fill(x, y, x + width, y + height, 0x90000000.toInt())
        val lines = cachedLines ?: return
        val font: Font = Minecraft.getInstance().font
        var curY: Int = y + 4
        for (line in lines) {
            g.drawString(font, line, x + 8, curY, 0xFFEEEEEE.toInt(), false)
            curY += 10
        }
    }

    override fun renderControl(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
    }
}