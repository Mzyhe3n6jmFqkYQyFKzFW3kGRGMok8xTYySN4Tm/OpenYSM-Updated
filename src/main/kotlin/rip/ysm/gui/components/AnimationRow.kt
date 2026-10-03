package rip.ysm.gui.components

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.resources.language.I18n
import net.minecraft.network.chat.Component
import rip.ysm.gui.ModernPlayerTextureScreen
import rip.ysm.gui.OptionRow

class AnimationRow : OptionRow<Any>() {
    @JvmField var animKey: String = null
    var owner: ModernPlayerTextureScreen = null
    constructor(x: Int, y: Int, width: Int, height: Int, animKey: String, owner: ModernPlayerTextureScreen) {
        super(x, y, width, height, null)
        this.animKey = animKey
        this.owner = owner
        var i18nKey: String = "gui.yes_steve_model.texture.button." + animKey.replace(':', '.')
        var label: Component = if (I18n.exists(i18nKey)) Component.translatable(i18nKey) else Component.literal(animKey)
        setMessage(label)
    }
    open fun matches(lowerSearch: String): Boolean {
        return animKey.toLowerCase().contains(lowerSearch) || getMessage().getString().toLowerCase().contains(lowerSearch)
    }
    open fun renderWidget(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        var selected: Boolean = animKey.equals(owner.currentAnimation())
        var bg: Int = if (selected) (0x90333333).toInt() else if (isHovered()) (0x90171717).toInt() else (0x90000000).toInt()
        g.fill(getX(), getY(), getX() + width, getY() + height, bg)
        if (selected) {
            g.fill(getX(), getY(), getX() + 2, getY() + height, -1)
        }
        var textY: Int = getY() + height - 8 / 2
        g.drawString(Minecraft.getInstance().font, getMessage(), getX() + 8, textY, -1, false)
    }
    open fun renderControl(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float)
    open fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        owner.selectAnimation(animKey)
    }
}