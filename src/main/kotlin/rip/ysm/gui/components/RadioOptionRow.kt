package rip.ysm.gui.components

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import rip.ysm.gui.Option
import rip.ysm.gui.OptionRow
import kotlin.math.max
import kotlin.math.min

open class RadioOptionRow(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    option: Option<Int>,
    private val labels: List<String>
) : OptionRow<Int>(x, y, width, height, option) {
    private var open: Boolean = false
    private var listScroll: Float = 0.0f

    override fun controlWidth(): Int = Mth.clamp(width / 2, 100, 220)

    override fun renderControl(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val cx = controlX()
        val cy = controlY()
        val cw = controlWidth()
        val ch = controlHeight()
        val hover = isMouseOverControl(mouseX.toDouble(), mouseY.toDouble())

        g.fill(cx, cy, cx + cw, cy + ch, blendBg(hover, 0x3EC8C8C8))
        g.renderOutline(cx, cy, cw, ch, 0x60FFFFFF)

        val text = Component.literal(labelAt(currentIndex()))
        g.drawString(Minecraft.getInstance().font, text, cx + 6, cy + (ch - 8) / 2, 0xFFFFFFFF.toInt(), false)

        val arrowX = cx + cw - 10
        val arrowY = cy + ch / 2 - 1
        g.fill(arrowX, arrowY, arrowX + 6, arrowY + 1, 0xFFCCCCCC.toInt())
        g.fill(arrowX + 1, arrowY + 1, arrowX + 5, arrowY + 2, 0xFFCCCCCC.toInt())
        g.fill(arrowX + 2, arrowY + 2, arrowX + 4, arrowY + 3, 0xFFCCCCCC.toInt())
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        if (!isMouseOverControl(event.x(), event.y())) return
        open = !open
        if (open) {
            val cur = currentIndex()
            val firstVisible = (listScroll / 14).toInt()
            if (cur < firstVisible || cur >= firstVisible + 8) {
                listScroll = max(0, min(cur, labels.size - 8)) * 14f
            }
        }
    }

    override fun isOverlayOpen(): Boolean = open

    override fun closeOverlay() {
        open = false
    }

    override fun renderOverlay(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float, scrollDisplay: Float) {
        if (!open || labels.isEmpty()) return
        val cx = controlX()
        val cw = controlWidth()
        val cy = controlY() - scrollDisplay.toInt()
        val ch = controlHeight()
        val visible = min(8, labels.size)
        val listH = visible * 14 + 2
        val listY = cy + ch

        g.nextStratum()
        g.pose().pushMatrix()
        g.fill(cx, listY, cx + cw, listY + listH, 0xFF111111.toInt())

        val first = max(0, min((listScroll / 14).toInt(), max(0, labels.size - visible)))

        for (i in 0 until visible) {
            val idx = first + i
            if (idx >= labels.size) break
            val itemY = listY + 1 + i * 14
            val hover = mouseX >= cx && mouseX < cx + cw && mouseY >= itemY && mouseY < itemY + 14
            val selected = idx == currentIndex()
            val bg = if (selected) 0x3CFFFFFF else if (hover) 0xFF333333.toInt() else 0
            if (bg != 0) {
                g.fill(cx + 1, itemY, cx + cw - 1, itemY + 14, bg)
            }
            g.drawString(
                Minecraft.getInstance().font,
                Component.literal(labelAt(idx)),
                cx + 6,
                itemY + (14 - 8) / 2,
                -1,
                true
            )
        }

        if (labels.size > visible) {
            val trackX = cx + cw - 3
            val trackTop = listY + 1
            val trackBot = listY + listH - 1
            val trackH = trackBot - trackTop
            val thumbH = max(8, trackH * visible / labels.size)
            val thumbY = trackTop + ((trackH - thumbH) * listScroll / max(1, (labels.size - visible) * 14)).toInt()
            g.fill(trackX, trackTop, trackX + 2, trackBot, 0x80444444.toInt())
            g.fill(trackX, thumbY, trackX + 2, thumbY + thumbH, 0xFFAAAAAA.toInt())
        }
        g.pose().popMatrix()
    }

    override fun overlayMouseClicked(mouseX: Double, mouseY: Double, button: Int, scrollDisplay: Float): Boolean {
        if (!open) return false
        val cx = controlX()
        val cw = controlWidth()
        val cy = controlY() - scrollDisplay.toInt()
        val ch = controlHeight()
        val visible = min(8, labels.size)
        val listH = visible * 14 + 2
        val listY = cy + ch
        if (mouseX < cx || mouseX >= cx + cw || mouseY < listY || mouseY >= listY + listH) {
            return false
        }
        val first = max(0, min((listScroll / 14).toInt(), max(0, labels.size - visible)))
        val slot = ((mouseY - listY - 1) / 14).toInt()
        val idx = first + slot
        if (idx in labels.indices) {
            option?.setPending(idx)
            open = false
        }
        return true
    }

    override fun overlayMouseScrolled(mouseX: Double, mouseY: Double, delta: Double, scrollDisplay: Float): Boolean {
        if (!open) return false
        val cx = controlX()
        val cw = controlWidth()
        val cy = controlY() - scrollDisplay.toInt()
        val ch = controlHeight()
        val visible = min(8, labels.size)
        val listH = visible * 14 + 2
        val listY = cy + ch
        if (mouseX < cx || mouseX >= cx + cw || mouseY < listY || mouseY >= listY + listH) return false
        val maxScroll = max(0, (labels.size - visible) * 14)
        listScroll = max(0.0, min(maxScroll.toDouble(), listScroll - delta * 14)).toFloat()
        return true
    }

    private fun labelAt(idx: Int): String {
        if (idx !in labels.indices) return ""
        return labels[idx]
    }

    private fun currentIndex(): Int {
        val cur = option?.get ?: return 0
        return Mth.clamp(cur, 0, max(0, labels.size - 1))
    }
}