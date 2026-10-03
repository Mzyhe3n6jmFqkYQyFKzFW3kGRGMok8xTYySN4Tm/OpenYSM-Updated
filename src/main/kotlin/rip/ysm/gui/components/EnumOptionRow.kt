package rip.ysm.gui.components

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import rip.ysm.gui.Option
import rip.ysm.gui.OptionRow
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

open class EnumOptionRow<E : Enum<E>>(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    option: Option<E>,
    private val values: Array<E>
) : OptionRow<E>(x, y, width, height, option) {

    private var open: Boolean = false
    private var listScroll: Float = 0.0f

    override fun controlWidth(): Int {
        return Mth.clamp(width / 2, 100, 220)
    }

    override fun renderControl(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val cx = controlX()
        val cy = controlY()
        val cw = controlWidth()
        val ch = controlHeight()
        val hover = isMouseOverControl(mouseX.toDouble(), mouseY.toDouble())

        g.fill(cx, cy, cx + cw, cy + ch, blendBg(hover, 0x3EC8C8C8))
        g.renderOutline(cx, cy, cw, ch, 0x60FFFFFF)

        val text = Component.literal(prettify(option?.get()?.name ?: ""))
        g.drawString(Minecraft.getInstance().font, text, cx + 6, cy + (ch - 8) / 2, -1, false)

        val arrowX = cx + cw - 10
        val arrowY = cy + ch / 2 - 1
        g.fill(arrowX, arrowY, arrowX + 6, arrowY + 1, 0xFFCCCCCC.toInt())
        g.fill(arrowX + 1, arrowY + 1, arrowX + 5, arrowY + 2, 0xFFCCCCCC.toInt())
        g.fill(arrowX + 2, arrowY + 2, arrowX + 4, arrowY + 3, 0xFFCCCCCC.toInt())
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        if (!isMouseOverControl(event.x(), event.y())) {
            return
        }
        open = !open
        if (open) {
            val cur = currentIndex()
            val firstVisible = (listScroll / 14).toInt()
            if (cur < firstVisible || cur >= firstVisible + 8) {
                listScroll = max(0, min(cur, values.size - 8)) * 14f
            }
        }
    }

    override fun isOverlayOpen(): Boolean {
        return open
    }

    override fun closeOverlay() {
        open = false
    }

    override fun renderOverlay(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float, scrollDisplay: Float) {
        if (!open) {
            return
        }
        val cx = controlX()
        val cw = controlWidth()
        val cy = controlY() - scrollDisplay.toInt()
        val ch = controlHeight()
        val visible = min(8, values.size)
        val listH = visible * 14 + 2
        val listX = cx
        val listY = cy + ch

        g.nextStratum()
        g.pose().pushMatrix()
        g.fill(listX, listY, listX + cw, listY + listH, 0xFF111111.toInt())

        var first = (listScroll / 14).toInt()
        first = max(0, min(first, max(0, values.size - visible)))

        for (i in 0 until visible) {
            val idx = first + i
            if (idx >= values.size) {
                break
            }
            val itemY = listY + 1 + i * 14
            val hover = mouseX >= listX && mouseX < listX + cw && mouseY >= itemY && mouseY < itemY + 14
            val selected = idx == currentIndex()
            val bg = if (selected) Color(255, 255, 255, 60).rgb else if (hover) 0xFF333333.toInt() else 0
            if (bg != 0) {
                g.fill(listX + 1, itemY, listX + cw - 1, itemY + 14, bg)
            }
            g.drawString(
                Minecraft.getInstance().font,
                Component.literal(prettify(values[idx].name)),
                listX + 6,
                itemY + (14 - 8) / 2,
                -1,
                true
            )
        }

        if (values.size > visible) {
            val trackX = listX + cw - 3
            val trackTop = listY + 1
            val trackBot = listY + listH - 1
            val trackH = trackBot - trackTop
            val thumbH = max(8, trackH * visible / values.size)
            val thumbY = trackTop + ((trackH - thumbH) * listScroll / max(1, (values.size - visible) * 14)).toInt()
            g.fill(trackX, trackTop, trackX + 2, trackBot, 0x80444444.toInt())
            g.fill(trackX, thumbY, trackX + 2, thumbY + thumbH, 0xFFAAAAAA.toInt())
        }
        g.pose().popMatrix()
    }

    override fun overlayMouseClicked(mouseX: Double, mouseY: Double, button: Int, scrollDisplay: Float): Boolean {
        if (!open) {
            return false
        }
        val cx = controlX()
        val cw = controlWidth()
        val cy = controlY() - scrollDisplay.toInt()
        val ch = controlHeight()
        val visible = min(8, values.size)
        val listH = visible * 14 + 2
        val listX = cx
        val listY = cy + ch
        if (mouseX < listX || mouseX >= listX + cw || mouseY < listY || mouseY >= listY + listH) {
            return false
        }
        var first = (listScroll / 14).toInt()
        first = max(0, min(first, max(0, values.size - visible)))
        val slot = ((mouseY - listY - 1) / 14).toInt()
        val idx = first + slot
        if (idx in values.indices) {
            option?.setPending(values[idx])
            open = false
        }
        return true
    }

    override fun overlayMouseScrolled(mouseX: Double, mouseY: Double, delta: Double, scrollDisplay: Float): Boolean {
        if (!open) {
            return false
        }
        val cx = controlX()
        val cw = controlWidth()
        val cy = controlY() - scrollDisplay.toInt()
        val ch = controlHeight()
        val visible = min(8, values.size)
        val listH = visible * 14 + 2
        val listX = cx
        val listY = cy + ch
        if (mouseX < listX || mouseX >= listX + cw || mouseY < listY || mouseY >= listY + listH) {
            return false
        }
        val maxScroll = max(0, (values.size - visible) * 14)
        listScroll = max(0.0, min(maxScroll.toDouble(), listScroll - delta * 14)).toFloat()
        return true
    }

    private fun currentIndex(): Int {
        val current = option?.get()
        for (i in values.indices) {
            if (values[i] == current) {
                return i
            }
        }
        return 0
    }

    companion object {
        @JvmStatic
        fun prettify(name: String): String {
            val parts = name.split("_")
            val sb = StringBuilder(name.length)
            for (i in parts.indices) {
                val p = parts[i]
                if (p.isEmpty()) {
                    continue
                }
                if (i > 0) {
                    sb.append(' ')
                }
                sb.append(p[0].uppercaseChar())
                if (p.length > 1) {
                    sb.append(p.substring(1).lowercase())
                }
            }
            return sb.toString()
        }
    }
}