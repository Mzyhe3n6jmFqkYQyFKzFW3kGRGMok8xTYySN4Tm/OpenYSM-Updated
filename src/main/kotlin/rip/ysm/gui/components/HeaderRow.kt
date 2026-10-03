package rip.ysm.gui.components

import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import rip.ysm.gui.OptionRow

class HeaderRow(val text: String) : OptionRow<Any?>(0, 0, 0, 22, null) {

    override fun renderWidget(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        g.fill(x, y, x + width, y + height, 0x90000000.toInt())
        val textY = y + (height - 8) / 2
        g.drawString(
            Minecraft.getInstance().font,
            Component.literal(text).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
            x + 8,
            textY,
            -1,
            false
        )
    }

    override fun renderControl(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
    }
}