package rip.ysm.gui.components

import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import rip.ysm.gui.OptionRow

class HeaderRow : OptionRow<Any>() {
    var text: String = null
    constructor(text: String) {
        super(0, 0, 0, 22, null)
        this.text = text
    }
    open fun renderWidget(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        g.fill(getX(), getY(), getX() + width, getY() + height, (0x90000000).toInt())
        g.drawString(Minecraft.getInstance().font, Component.literal(text).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), getX() + 8, getY() + height - 8 / 2, -1, false)
    }
    open fun renderControl(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float)
}