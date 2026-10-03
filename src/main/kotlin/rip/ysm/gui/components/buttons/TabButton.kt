package rip.ysm.gui.components.buttons

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.input.MouseButtonEvent
import rip.ysm.gui.OptionGroup
import java.util.function.Consumer

open class TabButton : AbstractWidget() {
    var group: OptionGroup = null
    var onSelect: Consumer<OptionGroup> = null
    var selected: Boolean = false
    var horizontal: Boolean = false
    constructor(x: Int, y: Int, width: Int, height: Int, group: OptionGroup, onSelect: Consumer<OptionGroup>) {
        super(x, y, width, height, group.getTitle())
        this.group = group
        this.onSelect = onSelect
    }
    open fun getGroup(): OptionGroup {
        return group
    }
    open fun setSelected(selected: Boolean) {
        this.selected = selected
    }
    open fun setHorizontal(horizontal: Boolean) {
        this.horizontal = horizontal
    }
    open fun renderWidget(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        var bg: Int = if (selected) (0x90171717).toInt() else if (isHovered()) (0x900B0B0B).toInt() else (0x90000000).toInt()
        g.fill(getX(), getY(), getX() + width, getY() + height, bg)
        if (selected) {
            if (horizontal) {
                g.fill(getX(), getY(), getX() + width, getY() + 1, -1)
            } else {
                g.fill(getX(), getY(), getX() + 1, getY() + height, -1)
            }
        }
        var textY: Int = getY() + height - 8 / 2
        if (horizontal) {
            var tw: Int = Minecraft.getInstance().font.width(getMessage())
            var textX: Int = getX() + Math.max(6, width - tw / 2)
            g.drawString(Minecraft.getInstance().font, getMessage(), textX, textY, (0xFFFFFFFF).toInt(), false)
        } else {
            g.drawString(Minecraft.getInstance().font, getMessage(), getX() + 10, textY, (0xFFFFFFFF).toInt(), false)
        }
    }
    open fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        onSelect.accept(group)
    }
    open fun updateWidgetNarration(out: NarrationElementOutput) {
        defaultButtonNarrationText(out)
    }
}