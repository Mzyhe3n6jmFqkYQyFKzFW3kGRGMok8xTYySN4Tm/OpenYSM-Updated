package rip.ysm.gui.components.buttons

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import java.awt.Color

open class FooterButton(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    label: Component,
    private val onPress: Runnable
) : AbstractWidget(x, y, width, height, label) {

    override fun renderWidget(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val bg = if (!active) 0x90282828.toInt() else if (isHovered) Color(
            0x90171717.toInt(),
            true
        ).rgb else 0x90000000.toInt()
        g.fill(x, y, x + width, y + height, bg)
        val font = Minecraft.getInstance().font
        val tw = font.width(message)
        val color = if (active) -0x1 else -0x777778 // 0xFFFFFFFF : 0xFF888888
        g.drawString(font, message, x + (width - tw) / 2, y + (height - 8) / 2, color, false)
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        if (active) {
            onPress.run()
        }
    }

    override fun updateWidgetNarration(out: NarrationElementOutput) {
        defaultButtonNarrationText(out)
    }
}