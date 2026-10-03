package rip.ysm.gui.components.buttons

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component

open class FooterButton(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    label: Component,
    private val onPress: () -> Unit
) : AbstractWidget(x, y, width, height, label) {

    override fun renderWidget(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val bg = if (!active) 0x90282828.toInt() else if (isHovered) 0x90171717.toInt() else 0x90000000.toInt()
        g.fill(x, y, x + width, y + height, bg)
        val font = Minecraft.getInstance().font
        val tw = font.width(message)
        val color = if (active) 0xFFFFFFFF.toInt() else 0xFF888888.toInt()
        g.drawString(font, message, x + (width - tw) / 2, y + (height - 8) / 2, color, false)
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        if (active) {
            onPress()
        }
    }

    override fun updateWidgetNarration(out: NarrationElementOutput) {
        defaultButtonNarrationText(out)
    }
}