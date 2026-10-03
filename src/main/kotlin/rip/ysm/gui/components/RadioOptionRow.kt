package rip.ysm.gui.components

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import rip.ysm.gui.Option
import rip.ysm.gui.OptionRow
import java.awt.*
import java.util.List

open class RadioOptionRow : OptionRow<Integer>() {
    var labels: MutableList<String> = null
    var open: Boolean = false
    var listScroll: Float = 0.0f
    constructor(x: Int, y: Int, width: Int, height: Int, option: Option<Integer>, labels: MutableList<String>) {
        super(x, y, width, height, option)
        this.labels = labels
    }
    open fun controlWidth(): Int {
        return Mth.clamp(width / 2, 100, 220)
    }
    open fun renderControl(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        var cx: Int = controlX()
        var cy: Int = controlY()
        var cw: Int = controlWidth()
        var ch: Int = controlHeight()
        var hover: Boolean = isMouseOverControl(mouseX, mouseY)
        g.fill(cx, cy, cx + cw, cy + ch, blendBg(hover, 0x3EC8C8C8))
        g.renderOutline(cx, cy, cw, ch, 0x60FFFFFF)
        var text: Component = Component.literal(labelAt(currentIndex()))
        g.drawString(Minecraft.getInstance().font, text, cx + 6, cy + ch - 8 / 2, (0xFFFFFFFF).toInt(), false)
        var arrowX: Int = cx + cw - 10
        var arrowY: Int = cy + ch / 2 - 1
        g.fill(arrowX, arrowY, arrowX + 6, arrowY + 1, (0xFFCCCCCC).toInt())
        g.fill(arrowX + 1, arrowY + 1, arrowX + 5, arrowY + 2, (0xFFCCCCCC).toInt())
        g.fill(arrowX + 2, arrowY + 2, arrowX + 4, arrowY + 3, (0xFFCCCCCC).toInt())
    }
    open fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        if (!isMouseOverControl(event.x(), event.y())) {
            return
        }
        open = !open
        if (open) {
            var cur: Int = currentIndex()
            var firstVisible: Int = (listScroll / 14 as Int)
            if (cur < firstVisible || cur >= firstVisible + 8) {
                listScroll = Math.max(0, Math.min(cur, labels.size() - 8)) * 14
            }
        }
    }
    open fun isOverlayOpen(): Boolean {
        return open
    }
    open fun closeOverlay() {
        open = false
    }
    open fun renderOverlay(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float, scrollDisplay: Float) {
        if (!open || labels.isEmpty()) {
            return
        }
        var cx: Int = controlX()
        var cw: Int = controlWidth()
        var cy: Int = controlY() - (scrollDisplay as Int)
        var ch: Int = controlHeight()
        var visible: Int = Math.min(8, labels.size())
        var listH: Int = visible * 14 + 2
        var listX: Int = cx
        var listY: Int = cy + ch
        g.nextStratum()
        g.pose().pushMatrix()
        g.fill(listX, listY, listX + cw, listY + listH, (0xFF111111).toInt())
        var first: Int = (listScroll / 14 as Int)
        first = Math.max(0, Math.min(first, Math.max(0, labels.size() - visible)))
        var i = 0
        while (i < visible) {
            var idx: Int = first + i
            if (idx >= labels.size()) {
                break
            }
            var itemY: Int = listY + 1 + i * 14
            var hover: Boolean = mouseX >= listX && mouseX < listX + cw && mouseY >= itemY && mouseY < itemY + 14
            var selected: Boolean = idx == currentIndex()
            var bg: Int = if (selected) Color(255, 255, 255, 60).getRGB() else if (hover) (0xFF333333).toInt() else 0
            if (bg != 0) {
                g.fill(listX + 1, itemY, listX + cw - 1, itemY + 14, bg)
            }
            g.drawString(Minecraft.getInstance().font, Component.literal(labelAt(idx)), listX + 6, itemY + 14 - 8 / 2, -1, true)
            i++
        }
        if (labels.size() > visible) {
            var trackX: Int = listX + cw - 3
            var trackTop: Int = listY + 1
            var trackBot: Int = listY + listH - 1
            var trackH: Int = trackBot - trackTop
            var thumbH: Int = Math.max(8, trackH * visible / labels.size())
            var thumbY: Int = trackTop + (trackH - thumbH * listScroll / Math.max(1, labels.size() - visible * 14) as Int)
            g.fill(trackX, trackTop, trackX + 2, trackBot, (0x80444444).toInt())
            g.fill(trackX, thumbY, trackX + 2, thumbY + thumbH, (0xFFAAAAAA).toInt())
        }
        g.pose().popMatrix()
    }
    open fun overlayMouseClicked(mouseX: Double, mouseY: Double, button: Int, scrollDisplay: Float): Boolean {
        if (!open) {
            return false
        }
        var cx: Int = controlX()
        var cw: Int = controlWidth()
        var cy: Int = controlY() - (scrollDisplay as Int)
        var ch: Int = controlHeight()
        var visible: Int = Math.min(8, labels.size())
        var listH: Int = visible * 14 + 2
        var listX: Int = cx
        var listY: Int = cy + ch
        if (mouseX < listX || mouseX >= listX + cw || mouseY < listY || mouseY >= listY + listH) {
            return false
        }
        var first: Int = (listScroll / 14 as Int)
        first = Math.max(0, Math.min(first, Math.max(0, labels.size() - visible)))
        var slot: Int = (mouseY - listY - 1 / 14 as Int)
        var idx: Int = first + slot
        if (idx >= 0 && idx < labels.size()) {
            option.setPending(idx)
            open = false
        }
        return true
    }
    open fun overlayMouseScrolled(mouseX: Double, mouseY: Double, delta: Double, scrollDisplay: Float): Boolean {
        if (!open) {
            return false
        }
        var cx: Int = controlX()
        var cw: Int = controlWidth()
        var cy: Int = controlY() - (scrollDisplay as Int)
        var ch: Int = controlHeight()
        var visible: Int = Math.min(8, labels.size())
        var listH: Int = visible * 14 + 2
        var listX: Int = cx
        var listY: Int = cy + ch
        if (mouseX < listX || mouseX >= listX + cw || mouseY < listY || mouseY >= listY + listH) {
            return false
        }
        var max: Int = Math.max(0, labels.size() - visible * 14)
        listScroll = (Math.max(0, Math.min(max, listScroll - delta * 14)) as Float)
        return true
    }
    open fun labelAt(idx: Int): String {
        if (idx < 0 || idx >= labels.size()) {
            return ""
        }
        return labels.get(idx)
    }
    open fun currentIndex(): Int {
        var cur: Integer = option.get()
        if (cur == null) {
            return 0
        }
        return Mth.clamp(cur, 0, Math.max(0, labels.size() - 1))
    }
}