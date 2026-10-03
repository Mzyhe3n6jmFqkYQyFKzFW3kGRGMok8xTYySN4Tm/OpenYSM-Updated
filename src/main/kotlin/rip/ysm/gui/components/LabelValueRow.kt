package rip.ysm.gui.components

import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import rip.ysm.gui.OptionRow

class LabelValueRow(val labelKey: String, val value: String) : OptionRow<Any?>(0, 0, 0, 18, null) {

    override fun renderWidget(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        g.fill(x, y, x + width, y + height, 0x90000000.toInt())
        val label = Component.translatable(labelKey).withStyle(ChatFormatting.AQUA)
        val textY = y + (height - 8) / 2
        g.drawString(Minecraft.getInstance().font, label, x + 8, textY, -1, false)
        val labelW = Minecraft.getInstance().font.width(label)
        g.drawString(
            Minecraft.getInstance().font,
            Component.literal(value),
            x + 8 + labelW + 6,
            textY,
            0xFFCCCCCC.toInt(),
            false
        )
    }

    override fun renderControl(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
    }
}