package rip.ysm.gui

import com.elfmcys.yesstevemodel.config.GeneralConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.minecraft.util.FormattedCharSequence
import net.minecraft.util.Mth
import org.jetbrains.annotations.Nullable
import rip.ysm.gpu.BlurStack
import rip.ysm.gui.components.buttons.FooterButton
import rip.ysm.gui.components.buttons.TabButton
import java.util.ArrayList
import java.util.HashMap
import java.util.List
import java.util.Map

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
    var lastFrameNanos: Long = 0L
    var draggingRowScrollbar: Boolean = false
    var draggingTabScrollbar: Boolean = false
    var applyBtn: FooterButton? = null
    var undoBtn: FooterButton? = null
    var saveBtn: FooterButton? = null
    var cancelBtn: FooterButton? = null
    abstract fun registerGroups()
    open fun init() {
        groups.clear()
        tabButtons.clear()
        activeRows.clear()
        registerGroups()
        var totalWidth: Int = computePanelWidth()
        var totalHeight: Int = computePanelHeight()
        panelLeft = this.width - totalWidth / 2
        panelTop = this.height - totalHeight / 2
        panelRight = panelLeft + totalWidth
        panelBottom = panelTop + totalHeight
        compactTabs = shouldUseCompactTabs()
        var tabs: Boolean = showTabs()
        if (!tabs) {
            tabAreaLeft = panelLeft
            tabAreaRight = panelLeft
            tabAreaTop = panelTop + 6 + 18
            tabAreaBottom = tabAreaTop
            rowAreaLeft = panelLeft
            rowAreaTop = panelTop + 6 + 18
            rowAreaRight = computeRowAreaRight()
            rowAreaBottom = panelBottom - 60
        } else {
            if (compactTabs) {
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
        }
        tabContentHeight = 0
        tabContentWidth = 0
        if (tabs && compactTabs) {
            var tabX: Int = tabAreaLeft
            for (g in groups) {
                var textW: Int = this.font.width(g.getTitle())
                var w: Int = Mth.clamp(textW + 16, 60, 140)
                var tb: TabButton = TabButton(tabX, tabAreaTop, w, 22, g, this::selectGroup)
                tb.setHorizontal(true)
                tabButtons.add(tb)
                tabX += w + 2
            }
            tabContentWidth = tabX - tabAreaLeft
            maxTabScroll = Math.max(0, tabContentWidth - tabAreaRight - tabAreaLeft)
        } else {
            if (tabs) {
                var tabY: Int = tabAreaTop
                for (g in groups) {
                    var tb: TabButton = TabButton(tabAreaLeft, tabY, 110, 22, g, this::selectGroup)
                    tabButtons.add(tb)
                    tabY += 22
                }
                tabContentHeight = tabY - tabAreaTop
                maxTabScroll = Math.max(0, tabContentHeight - tabAreaBottom - tabAreaTop)
            }
        }
        tabScrollOffset = 0
        tabScrollDisplay = 0
        var footerY: Int = panelBottom - 56
        var btnW: Int = 70
        var btnH: Int = 20
        var gap: Int = 4
        cancelBtn = FooterButton(panelRight - btnW, footerY, btnW, btnH, Component.translatable("gui.yes_steve_model.config.cancel"), this::onCancel)
        saveBtn = FooterButton(cancelBtn.getX() - btnW - gap, footerY, btnW, btnH, Component.translatable("gui.yes_steve_model.config.save"), this::onSave)
        applyBtn = FooterButton(saveBtn.getX() - btnW - gap, footerY, btnW, btnH, Component.translatable("gui.yes_steve_model.config.apply"), this::onApply)
        undoBtn = FooterButton(panelLeft, footerY, btnW, btnH, Component.translatable("gui.yes_steve_model.config.undo"), this::onUndo)
        addRenderableWidget(undoBtn)
        addRenderableWidget(applyBtn)
        addRenderableWidget(saveBtn)
        addRenderableWidget(cancelBtn)
        if (!groups.isEmpty()) {
            var toSelect: OptionGroup = groups.get(0)
            var stored: String = lastSelectedGroup.get(getClass())
            if (stored != null) {
                for (candidate in groups) {
                    if (stored.equals(candidate.getTranslationKey())) {
                        toSelect = candidate
                        break
                    }
                }
            }
            selectGroup(toSelect)
        }
    }
    open fun computePanelWidth(): Int {
        return Math.min(this.width - 40, 540)
    }
    open fun computePanelHeight(): Int {
        return Math.min(this.height - 40, 320)
    }
    open fun computeRowAreaRight(): Int {
        return panelRight
    }
    open fun shouldUseCompactTabs(): Boolean {
        return this.width < 500
    }
    open fun showTabs(): Boolean {
        return true
    }
    open fun selectGroup(group: OptionGroup) {
        if (activeGroup == group && !activeRows.isEmpty()) {
            return
        }
        for (r in activeRows) {
            r.closeOverlay()
        }
        activeRows.clear()
        activeGroup = group
        lastSelectedGroup.put(getClass(), group.getTranslationKey())
        var selectedIndex: Int = -1
        var i = 0
        while (i < tabButtons.size()) {
            var tb: TabButton = tabButtons.get(i)
            tb.setSelected(tb.getGroup() == group)
            if (tb.getGroup() == group) {
                selectedIndex = i
            }
            i++
        }
        if (selectedIndex >= 0 && maxTabScroll > 0) {
            var sel: TabButton = tabButtons.get(selectedIndex)
            if (compactTabs) {
                var btnLeft: Int = sel.getX() - tabAreaLeft
                var btnRight: Int = btnLeft + sel.getWidth()
                var viewW: Int = tabAreaRight - tabAreaLeft
                if (btnLeft < tabScrollOffset) {
                    tabScrollOffset = btnLeft
                } else {
                    if (btnRight > tabScrollOffset + viewW) {
                        tabScrollOffset = btnRight - viewW
                    }
                }
            } else {
                var btnTop: Int = selectedIndex * 22
                var btnBot: Int = btnTop + 22
                var viewH: Int = tabAreaBottom - tabAreaTop
                if (btnTop < tabScrollOffset) {
                    tabScrollOffset = btnTop
                } else {
                    if (btnBot > tabScrollOffset + viewH) {
                        tabScrollOffset = btnBot - viewH
                    }
                }
            }
            tabScrollOffset = Mth.clamp(tabScrollOffset, 0, maxTabScroll)
        }
        var rowY: Int = rowAreaTop
        var rowW: Int = rowAreaRight - rowAreaLeft
        for (template in group.getRows()) {
            template.setX(rowAreaLeft)
            template.setY(rowY)
            template.setWidth(rowW)
            activeRows.add(template)
            rowY += template.getHeight() + 2
        }
        rowContentHeight = rowY - rowAreaTop
        maxRowScroll = Math.max(0, rowContentHeight - rowAreaBottom - rowAreaTop)
        rowScrollOffset = Math.min(rowScrollOffset, maxRowScroll)
        rowScrollDisplay = Math.min(rowScrollDisplay, maxRowScroll)
    }
    open fun anyDirty(): Boolean {
        for (g in groups) {
            if (g.isDirty()) {
                return true
            }
        }
        return false
    }
    open fun onApply() {
        for (g in groups) {
            g.apply()
        }
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
        if (activeGroup != null) {
            activeGroup.undo()
        }
    }
    open fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderPanelBackdrop(g)
        g.fill(panelLeft, panelTop, panelRight, panelTop + 18, (0x90000000).toInt())
        g.drawString(this.font, this.title, panelLeft + 6, panelTop + 5, (0xFFFFFFFF).toInt(), false)
        var now: Long = System.nanoTime()
        if (lastFrameNanos == 0L) {
            lastFrameNanos = now
        }
        var dt: Float = Math.min(0.1f, now - lastFrameNanos / 1.0e9f)
        lastFrameNanos = now
        var lerp: Float = 1.0f - (Math.exp(-dt * 18.0f) as Float)
        rowScrollDisplay += rowScrollOffset - rowScrollDisplay * lerp
        if (Math.abs(rowScrollOffset - rowScrollDisplay) < 0.5f) {
            rowScrollDisplay = rowScrollOffset
        }
        tabScrollDisplay += tabScrollOffset - tabScrollDisplay * lerp
        if (Math.abs(tabScrollOffset - tabScrollDisplay) < 0.5f) {
            tabScrollDisplay = tabScrollOffset
        }
        var descY: Int = panelBottom - 32
        var inRowArea: Boolean = mouseX >= rowAreaLeft && mouseX < rowAreaRight && mouseY >= rowAreaTop && mouseY < rowAreaBottom
        var adjMouseY: Int = if (inRowArea) mouseY + Math.round(rowScrollDisplay) else Integer.MIN_VALUE
        hoveredRow = null
        if (inRowArea) {
            for (row in activeRows) {
                if (mouseX >= row.getX() && mouseX < row.getX() + row.getWidth() && adjMouseY >= row.getY() && adjMouseY < row.getY() + row.getHeight()) {
                    hoveredRow = row
                    break
                }
            }
        }
        var dirty: Boolean = anyDirty()
        applyBtn.active = dirty
        undoBtn.active = activeGroup != null && activeGroup.isDirty()
        super.render(g, mouseX, mouseY, partialTick)
        if (!tabButtons.isEmpty()) {
            var inTabArea: Boolean = mouseX >= tabAreaLeft && mouseX < tabAreaRight && mouseY >= tabAreaTop && mouseY < tabAreaBottom
            var adjTabMouseX: Int = mouseX
            var adjTabMouseY: Int = mouseY
            if (compactTabs) {
                adjTabMouseX = if (inTabArea) mouseX + Math.round(tabScrollDisplay) else Integer.MIN_VALUE
            } else {
                adjTabMouseY = if (inTabArea) mouseY + Math.round(tabScrollDisplay) else Integer.MIN_VALUE
            }
            g.enableScissor(tabAreaLeft, tabAreaTop, tabAreaRight, tabAreaBottom)
            g.pose().pushMatrix()
            if (compactTabs) {
                g.pose().translate(-tabScrollDisplay, 0)
            } else {
                g.pose().translate(0, -tabScrollDisplay)
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
        g.pose().translate(0, -rowScrollDisplay)
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
    open fun renderExtras(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float)
    open fun collectBlurRegions(out: MutableList<IntArray>) {
        out.add(intArrayOf(panelLeft, panelTop, panelRight - panelLeft, 18))
        var tabScroll: Int = Math.round(tabScrollDisplay)
        for (tb in tabButtons) {
            if (compactTabs) {
                var x: Int = tb.getX() - tabScroll
                var xRight: Int = x + tb.getWidth()
                var left: Int = Math.max(x, tabAreaLeft)
                var right: Int = Math.min(xRight, tabAreaRight)
                if (right > left) {
                    out.add(intArrayOf(left, tb.getY(), right - left, tb.getHeight()))
                }
            } else {
                var y: Int = tb.getY() - tabScroll
                var yBot: Int = y + tb.getHeight()
                var top: Int = Math.max(y, tabAreaTop)
                var bot: Int = Math.min(yBot, tabAreaBottom)
                if (bot > top) {
                    out.add(intArrayOf(tb.getX(), top, tb.getWidth(), bot - top))
                }
            }
        }
        var rowScroll: Int = Math.round(rowScrollDisplay)
        for (row in activeRows) {
            var y: Int = row.getY() - rowScroll
            var yBot: Int = y + row.getHeight()
            var top: Int = Math.max(y, rowAreaTop)
            var bot: Int = Math.min(yBot, rowAreaBottom)
            if (bot > top) {
                out.add(intArrayOf(row.getX(), top, row.getWidth(), bot - top))
            }
        }
        addFooterRect(out, applyBtn)
        addFooterRect(out, undoBtn)
        addFooterRect(out, saveBtn)
        addFooterRect(out, cancelBtn)
        if (hoveredRow != null) {
            var descY: Int = panelBottom - 32
            out.add(intArrayOf(panelLeft, descY, panelRight - panelLeft, 28))
        }
    }
    open fun addFooterRect(out: MutableList<IntArray>, btn: FooterButton) {
        if (btn == null || !btn.visible) {
            return
        }
        out.add(intArrayOf(btn.getX(), btn.getY(), btn.getWidth(), btn.getHeight()))
    }
    open fun renderPanelBackdrop(g: GuiGraphics) {
        if (GeneralConfig.BLUR_GUI == null || !GeneralConfig.BLUR_GUI.get()) {
            return
        }
        var regions: MutableList<IntArray> = ArrayList()
        collectBlurRegions(regions)
        for (r in regions) {
            if (r[2] <= 0 || r[3] <= 0) {
                continue
            }
            BlurStack.pushBlur(r[0], r[1], r[2], r[3], 0.0f, 24.0f)
        }
        BlurStack.flush(g)
    }
    open fun renderRowScrollbar(g: GuiGraphics) {
        var trackX: Int = rowAreaRight - 1
        var trackTop: Int = rowAreaTop + 1
        var trackBot: Int = rowAreaBottom - 1
        var trackH: Int = trackBot - trackTop
        var areaH: Int = rowAreaBottom - rowAreaTop
        var thumbH: Int = Math.max(16, trackH * areaH / Math.max(1, rowContentHeight))
        var thumbY: Int = trackTop + (trackH - thumbH * rowScrollDisplay / Math.max(1, maxRowScroll) as Int)
        g.fill(trackX, thumbY, trackX + 1, thumbY + thumbH, if (draggingRowScrollbar) (0xFFFFFFFF).toInt() else (0xFFAAAAAA).toInt())
    }
    open fun renderTabScrollbar(g: GuiGraphics) {
        if (compactTabs) {
            var trackY: Int = tabAreaBottom - 1
            var trackLeft: Int = tabAreaLeft + 1
            var trackRight: Int = tabAreaRight - 1
            var trackW: Int = trackRight - trackLeft
            var areaW: Int = tabAreaRight - tabAreaLeft
            var thumbW: Int = Math.max(16, trackW * areaW / Math.max(1, tabContentWidth))
            var thumbX: Int = trackLeft + (trackW - thumbW * tabScrollDisplay / Math.max(1, maxTabScroll) as Int)
            g.fill(thumbX, trackY, thumbX + thumbW, trackY + 1, if (draggingTabScrollbar) (0xFFFFFFFF).toInt() else (0xFFAAAAAA).toInt())
            return
        }
        var trackX: Int = tabAreaRight - 1
        var trackTop: Int = tabAreaTop + 1
        var trackBot: Int = tabAreaBottom - 1
        var trackH: Int = trackBot - trackTop
        var areaH: Int = tabAreaBottom - tabAreaTop
        var thumbH: Int = Math.max(16, trackH * areaH / Math.max(1, tabContentHeight))
        var thumbY: Int = trackTop + (trackH - thumbH * tabScrollDisplay / Math.max(1, maxTabScroll) as Int)
        g.fill(trackX, thumbY, trackX + 1, thumbY + thumbH, if (draggingTabScrollbar) (0xFFFFFFFF).toInt() else (0xFFAAAAAA).toInt())
    }
    open fun renderDescription(g: GuiGraphics, descY: Int) {
        if (hoveredRow == null || hoveredRow.getOption() == null) {
            return
        }
        g.fill(panelLeft, descY, panelRight, descY + 28, (0x80000000).toInt())
        var opt: Option<*> = hoveredRow.getOption()
        var title: Component = opt.getLabel()
        g.drawString(this.font, title, panelLeft + 6, descY + 4, -1, false)
        var desc: Component = opt.getDescription()
        var maxWidth: Int = panelRight - panelLeft - 6 * 2
        var lines: MutableList<FormattedCharSequence> = this.font.split(desc, maxWidth)
        var lineY: Int = descY + 16
        var max: Int = Math.min(lines.size(), 28 - 16 / 10)
        var i = 0
        while (i < max) {
            g.drawString(this.font, lines.get(i), panelLeft + 6, lineY, (0xFFCCCCCC).toInt(), false)
            lineY += 10
            i++
        }
    }
    open fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        var mouseX: Double = event.x()
        var mouseY: Double = event.y()
        var button: Int = event.button()
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
            var adjX: Double = if (compactTabs) mouseX + tabScrollDisplay else mouseX
            var adjY: Double = if (compactTabs) mouseY else mouseY + tabScrollDisplay
            var adjEvent: MouseButtonEvent = MouseButtonEvent(adjX, adjY, event.buttonInfo())
            for (tb in tabButtons) {
                if (tb.mouseClicked(adjEvent, doubleClick)) {
                    return true
                }
            }
            return true
        }
        if (mouseX >= rowAreaLeft && mouseX < rowAreaRight && mouseY >= rowAreaTop && mouseY < rowAreaBottom) {
            var adjY: Double = mouseY + rowScrollDisplay
            var adjEvent: MouseButtonEvent = MouseButtonEvent(mouseX, adjY, event.buttonInfo())
            for (row in activeRows) {
                if (row.mouseClicked(adjEvent, doubleClick)) {
                    setFocused(row)
                    if (button == 0) {
                        setDragging(true)
                    }
                    return true
                }
            }
            return true
        }
        return super.mouseClicked(event, doubleClick)
    }
    open fun mouseDragged(event: MouseButtonEvent, dx: Double, dy: Double): Boolean {
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
    open fun mouseReleased(event: MouseButtonEvent): Boolean {
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
    open fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        var delta: Double = scrollY
        for (row in activeRows) {
            if (row.isOverlayOpen() && row.overlayMouseScrolled(mouseX, mouseY, delta, rowScrollDisplay)) {
                return true
            }
        }
        if (mouseX >= tabAreaLeft && mouseX < tabAreaRight && mouseY >= tabAreaTop && mouseY < tabAreaBottom) {
            tabScrollOffset = Mth.clamp((tabScrollOffset - delta * 20 as Int), 0, maxTabScroll)
            return true
        }
        if (mouseX >= rowAreaLeft && mouseX < rowAreaRight && mouseY >= rowAreaTop && mouseY < rowAreaBottom) {
            rowScrollOffset = Mth.clamp((rowScrollOffset - delta * 20 as Int), 0, maxRowScroll)
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }
    open fun isOnRowScrollbar(mouseX: Double, mouseY: Double): Boolean {
        var trackX: Int = rowAreaRight - 4
        return mouseX >= trackX && mouseX < trackX + 3 && mouseY >= rowAreaTop && mouseY < rowAreaBottom
    }
    open fun isOnTabScrollbar(mouseX: Double, mouseY: Double): Boolean {
        if (compactTabs) {
            var trackY: Int = tabAreaBottom - 4
            return mouseY >= trackY && mouseY < trackY + 3 && mouseX >= tabAreaLeft && mouseX < tabAreaRight
        }
        var trackX: Int = tabAreaRight - 4
        return mouseX >= trackX && mouseX < trackX + 3 && mouseY >= tabAreaTop && mouseY < tabAreaBottom
    }
    open fun updateRowScrollFromMouse(mouseY: Double) {
        var trackTop: Int = rowAreaTop + 1
        var trackBot: Int = rowAreaBottom - 1
        var t: Double = Mth.clamp(mouseY - trackTop / Math.max(1, trackBot - trackTop), 0.0, 1.0)
        rowScrollOffset = (t * maxRowScroll as Int)
    }
    open fun updateTabScrollFromMouse(mouseX: Double, mouseY: Double) {
        if (compactTabs) {
            var trackLeft: Int = tabAreaLeft + 1
            var trackRight: Int = tabAreaRight - 1
            var t: Double = Mth.clamp(mouseX - trackLeft / Math.max(1, trackRight - trackLeft), 0.0, 1.0)
            tabScrollOffset = (t * maxTabScroll as Int)
            return
        }
        var trackTop: Int = tabAreaTop + 1
        var trackBot: Int = tabAreaBottom - 1
        var t: Double = Mth.clamp(mouseY - trackTop / Math.max(1, trackBot - trackTop), 0.0, 1.0)
        tabScrollOffset = (t * maxTabScroll as Int)
    }
    open fun shouldCloseOnEsc(): Boolean {
        return true
    }
    open fun onClose() {
        onCancel()
    }
    open fun isPauseScreen(): Boolean {
        return false
    }
    companion object {
        @JvmField var lastSelectedGroup: MutableMap<Class<OptionScreen>? = null, String> = HashMap()
    }
}