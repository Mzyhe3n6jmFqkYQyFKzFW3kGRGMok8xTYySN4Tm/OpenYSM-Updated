package rip.ysm.gui.components

import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import rip.ysm.gui.OptionRow

class LabelValueRow : OptionRow<Any>() {
    var labelKey: String = null
    var value: String = null
    constructor(labelKey: String, value: String) {
        super(0, 0, 0, 18, null)
        this.labelKey = labelKey
        this.value = value
    }
    open fun renderWidget(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        g.fill(getX(), getY(), getX() + width, getY() + height, (0x90000000).toInt())
        var label: Component = Component.translatable(labelKey).withStyle(ChatFormatting.AQUA)
        g.drawString(Minecraft.getInstance().font, label, getX() + 8, getY() + height - 8 / 2, -1, false)
        var labelW: Int = Minecraft.getInstance().font.width(label)
        g.drawString(Minecraft.getInstance().font, Component.literal(value), getX() + 8 + labelW + 6, getY() + height - 8 / 2, (0xFFCCCCCC).toInt(), false)
    }
    open fun renderControl(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float)
}