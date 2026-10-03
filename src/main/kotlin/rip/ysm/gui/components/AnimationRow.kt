package rip.ysm.gui.components

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.resources.language.I18n
import net.minecraft.network.chat.Component
import rip.ysm.gui.ModernPlayerTextureScreen
import rip.ysm.gui.OptionRow

class AnimationRow(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    val animKey: String,
    private val owner: ModernPlayerTextureScreen
) : OptionRow<Any?>(x, y, width, height, null) {

    init {
        val i18nKey = "gui.yes_steve_model.texture.button." + animKey.replace(':', '.')
        val label = if (I18n.exists(i18nKey)) Component.translatable(i18nKey) else Component.literal(animKey)
        message = label
    }

    fun matches(lowerSearch: String): Boolean =
        animKey.lowercase().contains(lowerSearch) || message.string.lowercase().contains(lowerSearch)

    override fun renderWidget(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val selected = animKey == owner.currentAnimation
        val bg = if (selected) 0x90333333.toInt() else if (isHovered) 0x90171717.toInt() else 0x90000000.toInt()
        g.fill(x, y, x + width, y + height, bg)
        if (selected) {
            g.fill(x, y, x + 2, y + height, -1)
        }
        val textY = y + (height - 8) / 2
        g.drawString(Minecraft.getInstance().font, message, x + 8, textY, -1, false)
    }

    override fun renderControl(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        owner.selectAnimation(animKey)
    }
}