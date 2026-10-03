package rip.ysm.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth

abstract class OptionRow<T>(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    val option: Option<T>?
) : AbstractWidget(x, y, width, height, option?.getLabel() ?: Component.empty()) {

    open fun getOption(): Option<T>? {
        return option
    }

    open fun refresh() {
    }

    override fun renderWidget(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val dirty = option != null && option.isDirty()
        val bg = if (isHovered) 0x90171717.toInt() else if (dirty) 0x90060606.toInt() else 0x90000000.toInt()
        g.fill(x, y, x + width, y + height, bg)
        val label = message
        val textColor = if (dirty) -1 else 0x90FFFFFF.toInt()
        val textY = y + (height - 8) / 2
        g.drawString(Minecraft.getInstance().font, label, x + 8, textY, textColor, false)
        renderControl(g, mouseX, mouseY, partialTick)
    }

    protected abstract fun renderControl(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float)

    open fun controlX(): Int {
        return x + width - controlWidth() - 6
    }

    open fun controlY(): Int {
        return y + (height - controlHeight()) / 2
    }

    open fun controlWidth(): Int {
        return 90
    }

    open fun controlHeight(): Int {
        return Math.min(height - 4, 16)
    }

    open fun isMouseOverControl(mx: Double, my: Double): Boolean {
        val cx = controlX()
        val cy = controlY()
        return mx >= cx && mx < cx + controlWidth() && my >= cy && my < cy + controlHeight()
    }

    open fun isOverlayOpen(): Boolean {
        return false
    }

    open fun closeOverlay() {
    }

    open fun renderOverlay(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float, scrollDisplay: Float) {
    }

    open fun overlayMouseClicked(mouseX: Double, mouseY: Double, button: Int, scrollDisplay: Float): Boolean {
        return false
    }

    open fun overlayMouseScrolled(mouseX: Double, mouseY: Double, delta: Double, scrollDisplay: Float): Boolean {
        return false
    }

    override fun updateWidgetNarration(out: NarrationElementOutput) {
        defaultButtonNarrationText(out)
    }

    companion object {
        @JvmStatic
        fun blendBg(hover: Boolean, base: Int): Int {
            if (!hover) {
                return base
            }
            val a = (base ushr 24) and 0xFF
            val r = Mth.clamp(((base shr 16) and 0xFF) + 40, 0, 255)
            val gn = Mth.clamp(((base shr 8) and 0xFF) + 40, 0, 255)
            val b = Mth.clamp((base and 0xFF) + 40, 0, 255)
            return (a shl 24) or (r shl 16) or (gn shl 8) or b
        }
    }
}