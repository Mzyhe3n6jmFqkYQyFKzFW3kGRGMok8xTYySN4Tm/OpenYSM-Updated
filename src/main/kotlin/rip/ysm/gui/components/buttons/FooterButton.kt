package rip.ysm.gui.components.buttons

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import java.awt.*

open class FooterButton : AbstractWidget() {
    var onPress: Runnable = null
    constructor(x: Int, y: Int, width: Int, height: Int, label: Component, onPress: Runnable) {
        super(x, y, width, height, label)
        this.onPress = onPress
    }
    open fun renderWidget(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        var bg: Int = if (!active) (0x90282828).toInt() else if (isHovered()) Color((0x90171717).toInt(), true).getRGB() else (0x90000000).toInt()
        g.fill(getX(), getY(), getX() + width, getY() + height, bg)
        var tw: Int = Minecraft.getInstance().font.width(getMessage())
        var color: Int = if (active) (0xFFFFFFFF).toInt() else (0xFF888888).toInt()
        g.drawString(Minecraft.getInstance().font, getMessage(), getX() + width - tw / 2, getY() + height - 8 / 2, color, false)
    }
    open fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        if (active) {
            onPress.run()
        }
    }
    open fun updateWidgetNarration(out: NarrationElementOutput) {
        defaultButtonNarrationText(out)
    }
}