package rip.ysm.gui.components

import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.resources.language.I18n
import net.minecraft.network.chat.Component
import rip.ysm.gui.ModernModelInfoScreen
import rip.ysm.gui.OptionRow
import kotlin.math.max

class LinkRow(
    private val owner: ModernModelInfoScreen,
    val label: String,
    val url: String
) : OptionRow<Any?>(0, 0, 0, 20, null) {

    override fun renderWidget(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val hover = isHovered
        g.fill(x, y, x + width, y + height, if (hover) 0x90171717.toInt() else 0x90000000.toInt())
        val font: Font = Minecraft.getInstance().font
        val i18nKey = "gui.yes_steve_model.url.$label"
        val nameComponent = if (I18n.exists(i18nKey)) Component.translatable(i18nKey) else Component.literal(label)
        val textY = y + (height - 8) / 2
        g.drawString(
            font,
            nameComponent.copy().withStyle(ChatFormatting.AQUA, ChatFormatting.UNDERLINE),
            x + 8,
            textY,
            -1,
            false
        )
        val urlComp = Component.literal(url).withStyle(ChatFormatting.GRAY)
        val urlW = font.width(urlComp)
        val urlX = max(x + 8 + font.width(nameComponent) + 12, x + width - urlW - 8)
        g.drawString(font, urlComp, urlX, textY, -1, false)
    }

    override fun renderControl(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        owner.openUrlWithConfirm(url)
    }
}