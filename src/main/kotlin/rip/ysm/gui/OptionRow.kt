package rip.ysm.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth

abstract class OptionRow<T> : AbstractWidget() {
    var option: Option<T> = null
    constructor(x: Int, y: Int, width: Int, height: Int, option: Option<T>) {
        super(x, y, width, height, if (option == null) Component.empty() else option.getLabel())
        this.option = option
    }
    open fun getOption(): Option<T> {
        return option
    }
    open fun refresh()
    open fun renderWidget(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        var dirty: Boolean = option != null && option.isDirty()
        var bg: Int = if (isHovered()) (0x90171717).toInt() else if (dirty) (0x90060606).toInt() else (0x90000000).toInt()
        g.fill(getX(), getY(), getX() + width, getY() + height, bg)
        var label: Component = getMessage()
        var textColor: Int = if (dirty) -1 else (0x90FFFFFF).toInt()
        var textY: Int = getY() + height - 8 / 2
        g.drawString(Minecraft.getInstance().font, label, getX() + 8, textY, textColor, false)
        renderControl(g, mouseX, mouseY, partialTick)
    }
    abstract fun renderControl(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float)
    open fun controlX(): Int {
        return getX() + width - controlWidth() - 6
    }
    open fun controlY(): Int {
        return getY() + height - controlHeight() / 2
    }
    open fun controlWidth(): Int {
        return 90
    }
    open fun controlHeight(): Int {
        return Math.min(height - 4, 16)
    }
    open fun isMouseOverControl(mx: Double, my: Double): Boolean {
        var cx: Int = controlX()
        var cy: Int = controlY()
        return mx >= cx && mx < cx + controlWidth() && my >= cy && my < cy + controlHeight()
    }
    open fun isOverlayOpen(): Boolean {
        return false
    }
    open fun closeOverlay()
    open fun renderOverlay(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float, scrollDisplay: Float)
    open fun overlayMouseClicked(mouseX: Double, mouseY: Double, button: Int, scrollDisplay: Float): Boolean {
        return false
    }
    open fun overlayMouseScrolled(mouseX: Double, mouseY: Double, delta: Double, scrollDisplay: Float): Boolean {
        return false
    }
    open fun updateWidgetNarration(out: NarrationElementOutput) {
        defaultButtonNarrationText(out)
    }
    companion object {
        @JvmStatic fun blendBg(hover: Boolean, base: Int): Int {
            if (!hover) {
                return base
            }
            var a: Int = (base ushr 24 and 0xFF)
            var r: Int = Mth.clamp((base shr 16 and 0xFF) + 40, 0, 255)
            var gn: Int = Mth.clamp((base shr 8 and 0xFF) + 40, 0, 255)
            var b: Int = Mth.clamp((base and 0xFF) + 40, 0, 255)
            return (((a shl 24 or r shl 16) or gn shl 8) or b)
        }
    }
}