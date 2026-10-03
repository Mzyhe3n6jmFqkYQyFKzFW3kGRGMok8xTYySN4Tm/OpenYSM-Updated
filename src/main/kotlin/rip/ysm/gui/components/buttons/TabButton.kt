package rip.ysm.gui.components.buttons

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.input.MouseButtonEvent
import rip.ysm.gui.OptionGroup
import kotlin.math.max

open class TabButton(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    val group: OptionGroup,
    private val onSelect: (OptionGroup) -> Unit
) : AbstractWidget(x, y, width, height, group.getTitle()) {

    var selected: Boolean = false
    var horizontal: Boolean = false

    override fun renderWidget(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val bg = if (selected) 0x90171717.toInt() else if (isHovered) 0x900B0B0B.toInt() else 0x90000000.toInt()
        g.fill(x, y, x + width, y + height, bg)
        if (selected) {
            if (horizontal) {
                g.fill(x, y, x + width, y + 1, -1)
            } else {
                g.fill(x, y, x + 1, y + height, -1)
            }
        }
        val font = Minecraft.getInstance().font
        val textY = y + (height - 8) / 2
        if (horizontal) {
            val tw = font.width(message)
            val textX = x + max(6, (width - tw) / 2)
            g.drawString(font, message, textX, textY, -1, false)
        } else {
            g.drawString(font, message, x + 10, textY, -1, false)
        }
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        onSelect(group)
    }

    override fun updateWidgetNarration(out: NarrationElementOutput) {
        defaultButtonNarrationText(out)
    }
}