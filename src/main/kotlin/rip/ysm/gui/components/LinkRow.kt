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

class LinkRow : OptionRow<Any>() {
    var owner: ModernModelInfoScreen = null
    var label: String = null
    var url: String = null
    constructor(owner: ModernModelInfoScreen, label: String, url: String) {
        super(0, 0, 0, 20, null)
        this.owner = owner
        this.label = label
        this.url = url
    }
    open fun renderWidget(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        var hover: Boolean = isHovered()
        g.fill(getX(), getY(), getX() + width, getY() + height, if (hover) (0x90171717).toInt() else (0x90000000).toInt())
        var font: Font = Minecraft.getInstance().font
        var i18nKey: String = "gui.yes_steve_model.url." + label
        var nameComponent: Component = if (I18n.exists(i18nKey)) Component.translatable(i18nKey) else Component.literal(label)
        g.drawString(font, nameComponent.copy().withStyle(ChatFormatting.AQUA, ChatFormatting.UNDERLINE), getX() + 8, getY() + height - 8 / 2, -1, false)
        var urlComp: Component = Component.literal(url).withStyle(ChatFormatting.GRAY)
        var urlW: Int = font.width(urlComp)
        var urlX: Int = Math.max(getX() + 8 + font.width(nameComponent) + 12, getX() + width - urlW - 8)
        g.drawString(font, urlComp, urlX, getY() + height - 8 / 2, -1, false)
    }
    open fun renderControl(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float)
    open fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        owner.openUrlWithConfirm(url)
    }
}