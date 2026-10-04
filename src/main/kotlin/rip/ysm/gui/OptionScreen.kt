package rip.ysm.gui

import com.elfmcys.yesstevemodel.config.GeneralConfig
import com.elfmcys.yesstevemodel.config.ServerConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import rip.ysm.gpu.BlurStack
import rip.ysm.gui.components.buttons.FooterButton
import rip.ysm.gui.components.buttons.TabButton
import kotlin.math.*

abstract class OptionScreen(title: Component, var parentScreen: Screen? = null) : Screen(title) {
    val groups: MutableList<OptionGroup> = ArrayList()
    val tabButtons: MutableList<TabButton> = ArrayList()
    val activeRows: MutableList<OptionRow<*>> = ArrayList()
    var activeGroup: OptionGroup? = null
    var hoveredRow: OptionRow<*>? = null

    var panelLeft: Int = 0
    var panelTop: Int = 0
    var panelRight: Int = 0
    var panelBottom: Int = 0

    var tabAreaLeft: Int = 0
    var tabAreaTop: Int = 0
    var tabAreaRight: Int = 0
    var tabAreaBottom: Int = 0

    var rowAreaLeft: Int = 0
    var rowAreaTop: Int = 0
    var rowAreaRight: Int = 0
    var rowAreaBottom: Int = 0

    var rowScrollOffset: Int = 0
    var rowScrollDisplay: Float = 0.0f
    var maxRowScroll: Int = 0
    var rowContentHeight: Int = 0

    var tabScrollOffset: Int = 0
    var tabScrollDisplay: Float = 0.0f
    var maxTabScroll: Int = 0
    var tabContentHeight: Int = 0
    var tabContentWidth: Int = 0

    var compactTabs: Boolean = false
    private var lastFrameNanos: Long = 0L
    private var draggingRowScrollbar: Boolean = false
    private var draggingTabScrollbar: Boolean = false

    lateinit var applyBtn: FooterButton
    lateinit var undoBtn: FooterButton
    lateinit var saveBtn: FooterButton
    lateinit var cancelBtn: FooterButton

    protected abstract fun registerGroups()

    override fun init() {
        groups.clear()
        tabButtons.clear()
        activeRows.clear()
        registerGroups()

        val totalWidth = computePanelWidth()
        val totalHeight = computePanelHeight()
        panelLeft = (width - totalWidth) / 2
        panelTop = (height - totalHeight) / 2
        panelRight = panelLeft + totalWidth
        panelBottom = panelTop + totalHeight

        compactTabs = shouldUseCompactTabs()
        val tabs = showTabs()

        if (!tabs) {
            tabAreaLeft = panelLeft
            tabAreaRight = panelLeft
            tabAreaTop = panelTop + 6 + 18
            tabAreaBottom = tabAreaTop

            rowAreaLeft = panelLeft
            rowAreaTop = panelTop + 6 + 18
            rowAreaRight = computeRowAreaRight()
            rowAreaBottom = panelBottom - 60
        } else if (compactTabs) {
            tabAreaLeft = panelLeft
            tabAreaRight = panelRight
            tabAreaTop = panelTop + 6 + 18
            tabAreaBottom = tabAreaTop + 22

            rowAreaLeft = panelLeft
            rowAreaTop = tabAreaBottom + 4
            rowAreaRight = computeRowAreaRight()
            rowAreaBottom = panelBottom - 60
        } else {
            tabAreaLeft = panelLeft
            tabAreaTop = panelTop + 6 + 18
            tabAreaRight = panelLeft + 110
            tabAreaBottom = panelBottom - 60

            rowAreaLeft = panelLeft + 110 + 6
            rowAreaTop = panelTop + 6 + 18
            rowAreaRight = computeRowAreaRight()
            rowAreaBottom = panelBottom - 60
        }

        tabContentHeight = 0
        tabContentWidth = 0
        if (tabs && compactTabs) {
            var tabX = tabAreaLeft
            for (g in groups) {
                val textW = font.width(g.getTitle())
                val w = Mth.clamp(textW + 16, 60, 140)
                val tb = TabButton(tabX, tabAreaTop, w, 22, g, ::selectGroup)
                tb.horizontal = true
                tabButtons.add(tb)
                tabX += w + 2
            }
            tabContentWidth = tabX - tabAreaLeft
            maxTabScroll = max(0, tabContentWidth - (tabAreaRight - tabAreaLeft))
        } else if (tabs) {
            var tabY = tabAreaTop
            for (g in groups) {
                val tb = TabButton(tabAreaLeft, tabY, 110, 22, g, ::selectGroup)
                tabButtons.add(tb)
                tabY += 22
            }
            tabContentHeight = tabY - tabAreaTop
            maxTabScroll = max(0, tabContentHeight - (tabAreaBottom - tabAreaTop))
        }
        tabScrollOffset = 0
        tabScrollDisplay = 0f

        val footerY = panelBottom - 56
        val btnW = 70
        val btnH = 20
        val gap = 4
        cancelBtn = FooterButton(
            panelRight - btnW,
            footerY,
            btnW,
            btnH,
            Component.translatable("gui.yes_steve_model.config.cancel"),
            ::onCancel
        )
        saveBtn = FooterButton(
            cancelBtn.x - btnW - gap,
            footerY,
            btnW,
            btnH,
            Component.translatable("gui.yes_steve_model.config.save"),
            ::onSave
        )
        applyBtn = FooterButton(
            saveBtn.x - btnW - gap,
            footerY,
            btnW,
            btnH,
            Component.translatable("gui.yes_steve_model.config.apply"),
            ::onApply
        )
        undoBtn = FooterButton(
            panelLeft,
            footerY,
            btnW,
            btnH,
            Component.translatable("gui.yes_steve_model.config.undo"),
            ::onUndo
        )
        addRenderableWidget(undoBtn)
        addRenderableWidget(applyBtn)
        addRenderableWidget(saveBtn)
        addRenderableWidget(cancelBtn)

        if (groups.isNotEmpty()) {
            var toSelect = groups[0]
            val stored = lastSelectedGroup[javaClass]
            if (stored != null) {
                for (candidate in groups) {
                    if (stored == candidate.translationKey) {
                        toSelect = candidate
                        break
                    }
                }
            }
            selectGroup(toSelect)
        }
    }

    open fun computePanelWidth(): Int = min(width - 40, 540)

    open fun computePanelHeight(): Int = min(height - 40, 320)

    open fun computeRowAreaRight(): Int = panelRight

    open fun shouldUseCompactTabs(): Boolean = width < 500

    open fun showTabs(): Boolean = true

    open fun selectGroup(group: OptionGroup) {
        if (activeGroup == group && activeRows.isNotEmpty()) return
        for (r in activeRows) r.closeOverlay()
        activeRows.clear()
        activeGroup = group
        lastSelectedGroup[javaClass] = group.translationKey
        var selectedIndex = -1
        for (i in tabButtons.indices) {
            val tb = tabButtons[i]
            val isSelected = tb.group == group
            tb.selected = isSelected
            if (isSelected) selectedIndex = i
        }
        if (selectedIndex >= 0 && maxTabScroll > 0) {
            val sel = tabButtons[selectedIndex]
            if (compactTabs) {
                val btnLeft = sel.x - tabAreaLeft
                val btnRight = btnLeft + sel.width
                val viewW = tabAreaRight - tabAreaLeft
                if (btnLeft < tabScrollOffset) {
                    tabScrollOffset = btnLeft
                } else if (btnRight > tabScrollOffset + viewW) {
                    tabScrollOffset = btnRight - viewW
                }
            } else {
                val btnTop = selectedIndex * 22
                val btnBot = btnTop + 22
                val viewH = tabAreaBottom - tabAreaTop
                if (btnTop < tabScrollOffset) {
                    tabScrollOffset = btnTop
                } else if (btnBot > tabScrollOffset + viewH) {
                    tabScrollOffset = btnBot - viewH
                }
            }
            tabScrollOffset = Mth.clamp(tabScrollOffset, 0, maxTabScroll)
        }
        var rowY = rowAreaTop
        val rowW = rowAreaRight - rowAreaLeft
        for (template in group.rows) {
            template.x = rowAreaLeft
            template.y = rowY
            template.width = rowW
            activeRows.add(template)
            rowY += template.height + 2
        }
        rowContentHeight = rowY - rowAreaTop
        maxRowScroll = max(0, rowContentHeight - (rowAreaBottom - rowAreaTop))
        rowScrollOffset = min(rowScrollOffset, maxRowScroll)
        rowScrollDisplay = min(rowScrollDisplay, maxRowScroll.toFloat())
    }

    open fun anyDirty(): Boolean = groups.any { it.isDirty() }

    open fun onApply() {
        for (g in groups) {
            g.apply()
        }
        GeneralConfig.save()
        ServerConfig.save()
    }

    open fun onSave() {
        onApply()
        Minecraft.getInstance().setScreen(parentScreen)
    }

    open fun onCancel() {
        for (g in groups) {
            g.undo()
        }
        Minecraft.getInstance().setScreen(parentScreen)
    }

    open fun onUndo() {
        activeGroup?.undo()
    }

    override fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderPanelBackdrop(g)
        g.fill(panelLeft, panelTop, panelRight, panelTop + 18, 0x90000000.toInt())
        g.drawString(font, title, panelLeft + 6, panelTop + 5, 0xFFFFFFFF.toInt(), false)
        val now = System.nanoTime()
        if (lastFrameNanos == 0L) {
            lastFrameNanos = now
        }
        val dt = min(0.1f, (now - lastFrameNanos) / 1.0e9f)
        lastFrameNanos = now
        val lerp = 1.0f - exp(-dt * 18.0f)
        rowScrollDisplay += (rowScrollOffset - rowScrollDisplay) * lerp
        if (abs(rowScrollOffset - rowScrollDisplay) < 0.5f) {
            rowScrollDisplay = rowScrollOffset.toFloat()
        }
        tabScrollDisplay += (tabScrollOffset - tabScrollDisplay) * lerp
        if (abs(tabScrollOffset - tabScrollDisplay) < 0.5f) {
            tabScrollDisplay = tabScrollOffset.toFloat()
        }
        val descY = panelBottom - 32
        val inRowArea = mouseX in rowAreaLeft until rowAreaRight && mouseY in rowAreaTop until rowAreaBottom
        val adjMouseY = if (inRowArea) mouseY + rowScrollDisplay.roundToInt() else Int.MIN_VALUE
        hoveredRow = null
        if (inRowArea) {
            for (row in activeRows) {
                if (mouseX in row.x until (row.x + row.width) && adjMouseY in row.y until (row.y + row.height)) {
                    hoveredRow = row
                    break
                }
            }
        }
        val dirty = anyDirty()
        applyBtn.active = dirty
        undoBtn.active = activeGroup?.isDirty() == true
        super.render(g, mouseX, mouseY, partialTick)
        if (tabButtons.isNotEmpty()) {
            val inTabArea = mouseX in tabAreaLeft until tabAreaRight && mouseY in tabAreaTop until tabAreaBottom
            val adjTabMouseX =
                if (compactTabs) (if (inTabArea) mouseX + tabScrollDisplay.roundToInt() else Int.MIN_VALUE) else mouseX
            val adjTabMouseY =
                if (!compactTabs) (if (inTabArea) mouseY + tabScrollDisplay.roundToInt() else Int.MIN_VALUE) else mouseY
            g.enableScissor(tabAreaLeft, tabAreaTop, tabAreaRight, tabAreaBottom)
            g.pose().pushMatrix()
            if (compactTabs) {
                g.pose().translate(-tabScrollDisplay, 0f)
            } else {
                g.pose().translate(0f, -tabScrollDisplay)
            }
            for (tb in tabButtons) {
                tb.render(g, adjTabMouseX, adjTabMouseY, partialTick)
            }
            g.pose().popMatrix()
            g.disableScissor()
            if (maxTabScroll > 0) {
                renderTabScrollbar(g)
            }
        }
        g.enableScissor(rowAreaLeft, rowAreaTop, rowAreaRight, rowAreaBottom)
        g.pose().pushMatrix()
        g.pose().translate(0f, -rowScrollDisplay)
        for (row in activeRows) {
            row.render(g, mouseX, adjMouseY, partialTick)
        }
        g.pose().popMatrix()
        g.disableScissor()
        if (maxRowScroll > 0) {
            renderRowScrollbar(g)
        }
        renderDescription(g, descY)
        renderExtras(g, mouseX, mouseY, partialTick)
        for (row in activeRows) {
            if (row.isOverlayOpen()) {
                row.renderOverlay(g, mouseX, mouseY, partialTick, rowScrollDisplay)
            }
        }
    }

    open fun renderExtras(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
    }

    open fun collectBlurRegions(out: MutableList<IntArray>) {
        out.add(intArrayOf(panelLeft, panelTop, panelRight - panelLeft, 18))
        val tabScroll = tabScrollDisplay.roundToInt()
        for (tb in tabButtons) {
            if (compactTabs) {
                val x = tb.x - tabScroll
                val xRight = x + tb.width
                val left = max(x, tabAreaLeft)
                val right = min(xRight, tabAreaRight)
                if (right > left) {
                    out.add(intArrayOf(left, tb.y, right - left, tb.height))
                }
            } else {
                val y = tb.y - tabScroll
                val yBot = y + tb.height
                val top = max(y, tabAreaTop)
                val bot = min(yBot, tabAreaBottom)
                if (bot > top) {
                    out.add(intArrayOf(tb.x, top, tb.width, bot - top))
                }
            }
        }
        val rowScroll = rowScrollDisplay.roundToInt()
        for (row in activeRows) {
            val y = row.y - rowScroll
            val yBot = y + row.height
            val top = max(y, rowAreaTop)
            val bot = min(yBot, rowAreaBottom)
            if (bot > top) {
                out.add(intArrayOf(row.x, top, row.width, bot - top))
            }
        }
        addFooterRect(out, applyBtn)
        addFooterRect(out, undoBtn)
        addFooterRect(out, saveBtn)
        addFooterRect(out, cancelBtn)
        if (hoveredRow != null) {
            val descY = panelBottom - 32
            out.add(intArrayOf(panelLeft, descY, panelRight - panelLeft, 28))
        }
    }

    open fun addFooterRect(out: MutableList<IntArray>, btn: FooterButton?) {
        if (btn == null || !btn.visible) {
            return
        }
        out.add(intArrayOf(btn.x, btn.y, btn.width, btn.height))
    }

    open fun renderPanelBackdrop(g: GuiGraphics) {
        if (GeneralConfig.BLUR_GUI.get() != true) {
            return
        }
        val regions: MutableList<IntArray> = ArrayList()
        collectBlurRegions(regions)
        for (r in regions) {
            if (r[2] <= 0 || r[3] <= 0) {
                continue
            }
            BlurStack.pushBlur(r[0].toFloat(), r[1].toFloat(), r[2].toFloat(), r[3].toFloat(), 0.0f, 24.0f)
        }
        BlurStack.flush(g)
    }

    open fun renderRowScrollbar(g: GuiGraphics) {
        val trackX = rowAreaRight - 1
        val trackTop = rowAreaTop + 1
        val trackBot = rowAreaBottom - 1
        val trackH = trackBot - trackTop
        val areaH = rowAreaBottom - rowAreaTop
        val thumbH = max(16, trackH * areaH / max(1, rowContentHeight))
        val thumbY = trackTop + ((trackH - thumbH) * rowScrollDisplay / max(1, maxRowScroll)).toInt()
        g.fill(
            trackX,
            thumbY,
            trackX + 1,
            thumbY + thumbH,
            if (draggingRowScrollbar) 0xFFFFFFFF.toInt() else 0xFFAAAAAA.toInt()
        )
    }

    open fun renderTabScrollbar(g: GuiGraphics) {
        if (compactTabs) {
            val trackY = tabAreaBottom - 1
            val trackLeft = tabAreaLeft + 1
            val trackRight = tabAreaRight - 1
            val trackW = trackRight - trackLeft
            val areaW = tabAreaRight - tabAreaLeft
            val thumbW = max(16, trackW * areaW / max(1, tabContentWidth))
            val thumbX = trackLeft + ((trackW - thumbW) * tabScrollDisplay / max(1, maxTabScroll)).toInt()
            g.fill(
                thumbX,
                trackY,
                thumbX + thumbW,
                trackY + 1,
                if (draggingTabScrollbar) 0xFFFFFFFF.toInt() else 0xFFAAAAAA.toInt()
            )
            return
        }
        val trackX = tabAreaRight - 1
        val trackTop = tabAreaTop + 1
        val trackBot = tabAreaBottom - 1
        val trackH = trackBot - trackTop
        val areaH = tabAreaBottom - tabAreaTop
        val thumbH = max(16, trackH * areaH / max(1, tabContentHeight))
        val thumbY = trackTop + ((trackH - thumbH) * tabScrollDisplay / max(1, maxTabScroll)).toInt()
        g.fill(
            trackX,
            thumbY,
            trackX + 1,
            thumbY + thumbH,
            if (draggingTabScrollbar) 0xFFFFFFFF.toInt() else 0xFFAAAAAA.toInt()
        )
    }

    open fun renderDescription(g: GuiGraphics, descY: Int) {
        val row = hoveredRow ?: return
        val opt = row.option ?: return
        g.fill(panelLeft, descY, panelRight, descY + 28, 0x80000000.toInt())
        val title = opt.getLabel()
        g.drawString(font, title, panelLeft + 6, descY + 4, -1, false)
        val desc = opt.getDescription()
        val maxWidth = panelRight - panelLeft - 6 * 2
        val lines = font.split(desc, maxWidth)
        var lineY = descY + 16
        val max = min(lines.size, (28 - 16) / 10)
        for (i in 0 until max) {
            g.drawString(font, lines[i], panelLeft + 6, lineY, 0xFFCCCCCC.toInt(), false)
            lineY += 10
        }
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val mouseX = event.x()
        val mouseY = event.y()
        val button = event.button()
        for (row in activeRows) {
            if (row.isOverlayOpen() && row.overlayMouseClicked(mouseX, mouseY, button, rowScrollDisplay)) {
                return true
            }
        }
        for (row in activeRows) {
            if (row.isOverlayOpen()) {
                row.closeOverlay()
            }
        }
        if (maxRowScroll > 0 && isOnRowScrollbar(mouseX, mouseY)) {
            draggingRowScrollbar = true
            updateRowScrollFromMouse(mouseY)
            return true
        }
        if (maxTabScroll > 0 && isOnTabScrollbar(mouseX, mouseY)) {
            draggingTabScrollbar = true
            updateTabScrollFromMouse(mouseX, mouseY)
            return true
        }
        if (mouseX >= tabAreaLeft && mouseX < tabAreaRight && mouseY >= tabAreaTop && mouseY < tabAreaBottom) {
            val adjX = if (compactTabs) mouseX + tabScrollDisplay else mouseX
            val adjY = if (compactTabs) mouseY else mouseY + tabScrollDisplay
            val adjEvent = MouseButtonEvent(adjX, adjY, event.buttonInfo())
            for (tb in tabButtons) {
                if (tb.mouseClicked(adjEvent, doubleClick)) {
                    return true
                }
            }
            return true
        }
        if (mouseX >= rowAreaLeft && mouseX < rowAreaRight && mouseY >= rowAreaTop && mouseY < rowAreaBottom) {
            val adjY = mouseY + rowScrollDisplay
            val adjEvent = MouseButtonEvent(mouseX, adjY, event.buttonInfo())
            for (row in activeRows) {
                if (row.mouseClicked(adjEvent, doubleClick)) {
                    focused = row
                    if (button == 0) {
                        isDragging = true
                    }
                    return true
                }
            }
            return true
        }
        return super.mouseClicked(event, doubleClick)
    }

    override fun mouseDragged(event: MouseButtonEvent, dx: Double, dy: Double): Boolean {
        if (draggingRowScrollbar) {
            updateRowScrollFromMouse(event.y())
            return true
        }
        if (draggingTabScrollbar) {
            updateTabScrollFromMouse(event.x(), event.y())
            return true
        }
        return super.mouseDragged(event, dx, dy)
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        if (draggingRowScrollbar) {
            draggingRowScrollbar = false
            return true
        }
        if (draggingTabScrollbar) {
            draggingTabScrollbar = false
            return true
        }
        return super.mouseReleased(event)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        for (row in activeRows) {
            if (row.isOverlayOpen() && row.overlayMouseScrolled(mouseX, mouseY, scrollY, rowScrollDisplay))
                return true
        }
        if (mouseX >= tabAreaLeft && mouseX < tabAreaRight && mouseY >= tabAreaTop && mouseY < tabAreaBottom) {
            tabScrollOffset = Mth.clamp((tabScrollOffset - scrollY * 20).toInt(), 0, maxTabScroll)
            return true
        }
        if (mouseX >= rowAreaLeft && mouseX < rowAreaRight && mouseY >= rowAreaTop && mouseY < rowAreaBottom) {
            rowScrollOffset = Mth.clamp((rowScrollOffset - scrollY * 20).toInt(), 0, maxRowScroll)
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    private fun isOnRowScrollbar(mouseX: Double, mouseY: Double): Boolean {
        val trackX = rowAreaRight - 4
        return mouseX >= trackX && mouseX < trackX + 3 && mouseY >= rowAreaTop && mouseY < rowAreaBottom
    }

    private fun isOnTabScrollbar(mouseX: Double, mouseY: Double): Boolean {
        if (compactTabs) {
            val trackY = tabAreaBottom - 4
            return mouseY >= trackY && mouseY < trackY + 3 && mouseX >= tabAreaLeft && mouseX < tabAreaRight
        }
        val trackX = tabAreaRight - 4
        return mouseX >= trackX && mouseX < trackX + 3 && mouseY >= tabAreaTop && mouseY < tabAreaBottom
    }

    private fun updateRowScrollFromMouse(mouseY: Double) {
        val trackTop = rowAreaTop + 1
        val trackBot = rowAreaBottom - 1
        val t = Mth.clamp((mouseY - trackTop) / max(1, trackBot - trackTop), 0.0, 1.0)
        rowScrollOffset = (t * maxRowScroll).toInt()
    }

    private fun updateTabScrollFromMouse(mouseX: Double, mouseY: Double) {
        if (compactTabs) {
            val trackLeft = tabAreaLeft + 1
            val trackRight = tabAreaRight - 1
            val t = Mth.clamp((mouseX - trackLeft) / max(1, trackRight - trackLeft), 0.0, 1.0)
            tabScrollOffset = (t * maxTabScroll).toInt()
            return
        }
        val trackTop = tabAreaTop + 1
        val trackBot = tabAreaBottom - 1
        val t = Mth.clamp((mouseY - trackTop) / max(1, trackBot - trackTop), 0.0, 1.0)
        tabScrollOffset = (t * maxTabScroll).toInt()
    }

    override fun shouldCloseOnEsc(): Boolean = true

    override fun onClose() = onCancel()

    override fun isPauseScreen(): Boolean = false

    companion object {
        @JvmField
        val lastSelectedGroup: MutableMap<Class<out OptionScreen>, String> = HashMap()
    }
}