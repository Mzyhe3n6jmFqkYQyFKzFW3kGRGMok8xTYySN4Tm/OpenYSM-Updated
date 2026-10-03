package rip.ysm.gui

import com.elfmcys.yesstevemodel.NameSpaces
import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.client.entity.PlayerPreviewEntity
import com.elfmcys.yesstevemodel.client.gui.PlayerModelScreen
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.texture.AbstractTexture
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.util.Mth
import rip.ysm.gui.components.AnimationRow
import rip.ysm.gui.components.TextureGrid
import rip.ysm.gui.components.buttons.FooterButton
import rip.ysm.gui.components.buttons.IconButton
import rip.ysm.gui.components.buttons.TabButton
import rip.ysm.gui.components.groups.CategoryGroup
import rip.ysm.gui.components.groups.TextureGroup
import java.util.ArrayList
import java.util.LinkedHashMap
import java.util.List
import java.util.Map

open class ModernPlayerTextureScreen : OptionScreen() {
    @JvmField var renderContext: ModelAssembly = null
    @JvmField var modelId: String = null
    @JvmField var textureMap: OrderedStringMap<String, AbstractTexture> = null
    var modelHolder: PlayerPreviewEntity = null
    val icons: MutableList<IconButton> = ArrayList()
    var hoveredIcon: IconButton = null
    var searchBox: EditBox = null
    var currentAnimation: String = StringPool.EMPTY
    var previewLeft: Int = 0
    var previewTop: Int = 0
    var previewRight: Int = 0
    var previewBottom: Int = 0
    var yaw: Float = 165.0f
    var pitch: Float = -5.0f
    var zoom: Float = 80.0f
    var offsetX: Float = 0.0f
    var offsetY: Float = -60.0f
    var showGround: Boolean = true
    var draggingPreview: Boolean = false
    var draggingButton: Int = -1
    constructor(parent: PlayerModelScreen, modelId: String, modelAssembly: ModelAssembly) {
        super(Component.translatable("gui.yes_steve_model.texture_screen.title"), parent)
        this.renderContext = modelAssembly
        this.modelId = modelId
        this.textureMap = modelAssembly.getAnimationBundle().getTextures()
        this.modelHolder = PlayerPreviewEntity()
    }
    open fun computePanelWidth(): Int {
        return Math.min(this.width - 40, 780)
    }
    open fun computePanelHeight(): Int {
        return Math.min(this.height - 40, 380)
    }
    open fun shouldUseCompactTabs(): Boolean {
        return this.width < 640
    }
    open fun computeRowAreaRight(): Int {
        return panelRight - previewWidth() - 6
    }
    open fun previewWidth(): Int {
        return if (compactTabs) 200 else 280
    }
    open fun registerGroups() {
        if (!textureMap.isEmpty()) {
            var tg: TextureGroup = TextureGroup()
            tg.add(TextureGrid(this))
            groups.add(tg)
        }
        var mainAnims: Object2ReferenceMap<String, Animation> = renderContext.getAnimationBundle().getMainAnimations()
        var buckets: MutableMap<String, MutableList<String>> = LinkedHashMap()
        for (e in mainAnims.entrySet()) {
            var name: String = e.getKey()
            if (name.startsWith("——")) {
                continue
            }
            var key: String = e.getValue().sourceKey
            if (key == null) {
                key = "misc"
            }
            buckets.computeIfAbsent(key, { k -> ArrayList() }).add(name)
        }
        var sortedCats: MutableList<String> = ArrayList(buckets.keySet())
        sortedCats.sort({ a, b -> if (ia < 0) { ia = Integer.MAX_VALUE }
if (ib < 0) { ib = Integer.MAX_VALUE }
if (ia != ib) { return Integer.compare(ia, ib) }
return a.compareTo(b) })
        for (cat in sortedCats) {
            var g: CategoryGroup = CategoryGroup(cat)
            for (name in buckets.get(cat)) {
                g.add(AnimationRow(0, 0, 0, 18, name, this))
            }
            groups.add(g)
        }
    }
    open fun init() {
        super.init()
        removeWidget(applyBtn)
        removeWidget(undoBtn)
        removeWidget(cancelBtn)
        applyBtn.visible = false
        undoBtn.visible = false
        cancelBtn.visible = false
        applyBtn.active = false
        undoBtn.active = false
        saveBtn.setMessage(Component.translatable("gui.yes_steve_model.config.done"))
        saveBtn.setX(panelRight - saveBtn.getWidth())
        previewLeft = panelRight - previewWidth()
        previewTop = rowAreaTop
        previewRight = panelRight
        previewBottom = panelBottom - 60
        icons.clear()
        var iconY: Int = panelTop
        var iconX: Int = panelRight - 18
        icons.add(IconButton(iconX, iconY, 18, 64, 16, {  -> this.currentAnimation = "idle" }, Component.translatable("gui.yes_steve_model.model.stop")))
        iconX -= 20
        icons.add(IconButton(iconX, iconY, 18, 48, 16, this::resetView, Component.translatable("gui.yes_steve_model.model.reset")))
        iconX -= 20
        icons.add(IconButton(iconX, iconY, 18, 64, 0, {  -> this.showGround = !this.showGround }, Component.translatable("gui.yes_steve_model.model.ground")))
        var searchW: Int = Mth.clamp(panelRight - panelLeft - 3 * 18 - 2 * 2 - 200, 80, 140)
        var searchX: Int = iconX - 2 - searchW
        var oldQuery: String = if (searchBox != null) searchBox.getValue() else ""
        searchBox = EditBox(this.font, searchX, iconY, searchW, 18, Component.translatable("gui.yes_steve_model.search.placeholder"))
        searchBox.setTextColor(0xFFFFFF)
        searchBox.setHint(Component.translatable("gui.yes_steve_model.search.placeholder"))
        searchBox.setMaxLength(64)
        searchBox.setValue(oldQuery)
        searchBox.setResponder({ s -> applySearchFilter() })
        addRenderableWidget(searchBox)
    }
    open fun selectGroup(group: OptionGroup) {
        super.selectGroup(group)
        applySearchFilter()
    }
    open fun applySearchFilter() {
        if (activeGroup == null) {
            return
        }
        var s: String = if (searchBox != null) searchBox.getValue().toLowerCase().trim() else ""
        for (r in activeRows) {
            r.closeOverlay()
        }
        activeRows.clear()
        var rowY: Int = rowAreaTop
        var rowW: Int = rowAreaRight - rowAreaLeft
        for (template in activeGroup.getRows()) {
            if (!s.isEmpty() && template is AnimationRow && !ar.matches(s)) {
                continue
            }
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
    open fun resetView() {
        offsetX = 0.0f
        offsetY = -60.0f
        zoom = 80.0f
        yaw = 165.0f
        pitch = -5.0f
    }
    open fun selectAnimation(name: String) {
        this.currentAnimation = name
        if (!modelHolder.getAnimationStateMachine().isCurrentAnimation(name)) {
            modelHolder.getAnimationStateMachine().setCurrentAnimation(name)
        }
    }
    open fun currentAnimation(): String {
        return currentAnimation
    }
    open fun onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parentScreen)
        }
    }
    open fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        hoveredIcon = null
        for (btn in icons) {
            if (btn.contains(mouseX, mouseY)) {
                hoveredIcon = btn
                break
            }
        }
        super.render(g, mouseX, mouseY, partialTick)
        for (btn in icons) {
            drawIcon(g, btn)
        }
    }
    open fun drawIcon(g: GuiGraphics, btn: IconButton) {
        var hover: Boolean = btn == hoveredIcon
        var bg: Int = if (hover) (0x90171717).toInt() else (0x90000000).toInt()
        g.fill(btn.x, btn.y, btn.x + btn.size, btn.y + btn.size, bg)
        var ix: Int = btn.x + btn.size - 16 / 2
        var iy: Int = btn.y + btn.size - 16 / 2
        g.blit(RenderPipelines.GUI_TEXTURED, ICON_TEXTURE, ix, iy, btn.u, btn.v, 16, 16, 16, 16, 256, 256)
    }
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
        if (activeGroup is TextureGroup && !activeRows.isEmpty() && activeRows.get(0) is TextureGrid) {
            grid.collectBlurRegions(out, Math.round(rowScrollDisplay), rowAreaTop, rowAreaBottom)
        } else {
            var rowScroll: Int = Math.round(rowScrollDisplay)
            for (row in activeRows) {
                var y: Int = row.getY() - rowScroll
                var yBot: Int = y + row.getHeight()
                if (yBot <= rowAreaTop || y >= rowAreaBottom) {
                    continue
                }
                var top: Int = Math.max(y, rowAreaTop)
                var bot: Int = Math.min(yBot, rowAreaBottom)
                out.add(intArrayOf(row.getX(), top, row.getWidth(), bot - top))
            }
        }
        out.add(intArrayOf(previewLeft, previewTop, previewRight - previewLeft, previewBottom - previewTop))
        addFooterRect(out, applyBtn)
        addFooterRect(out, undoBtn)
        addFooterRect(out, saveBtn)
        addFooterRect(out, cancelBtn)
        for (btn in icons) {
            out.add(intArrayOf(btn.x, btn.y, btn.size, btn.size))
        }
        if (searchBox != null && searchBox.visible) {
            out.add(intArrayOf(searchBox.getX(), searchBox.getY(), searchBox.getWidth(), searchBox.getHeight()))
        }
        if (hoveredIcon != null || hoveredRow is AnimationRow) {
            var descY: Int = panelBottom - 32
            out.add(intArrayOf(panelLeft, descY, panelRight - panelLeft, 28))
        }
    }
    open fun renderExtras(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        g.fill(previewLeft, previewTop, previewRight, previewBottom, 0x66000000)
        renderPreview(g, partialTick)
    }
    open fun renderDescription(g: GuiGraphics, descY: Int) {
        if (hoveredIcon != null) {
            g.fill(panelLeft, descY, panelRight, descY + 28, (0x80000000).toInt())
            g.drawString(this.font, hoveredIcon.tooltip, panelLeft + 6, descY + 10, -1, false)
            return
        }
        if (hoveredRow is AnimationRow) {
            g.fill(panelLeft, descY, panelRight, descY + 28, (0x80000000).toInt())
            g.drawString(this.font, row.getMessage(), panelLeft + 6, descY + 4, -1, false)
            g.drawString(this.font, Component.literal(row.animKey).withStyle(ChatFormatting.GRAY), panelLeft + 6, descY + 16, (0xFFAAAAAA).toInt(), false)
        }
    }
    open fun renderPreview(g: GuiGraphics, partialTick: Float) {
        if (this.minecraft == null || this.minecraft.player == null) {
            return
        }
        if (!modelHolder.getAnimationStateMachine().isCurrentAnimation(currentAnimation)) {
            modelHolder.getAnimationStateMachine().setCurrentAnimation(currentAnimation)
        }
        PlayerCapability.get(this.minecraft.player).ifPresent({ cap -> modelHolder.initModelWithTexture(modelId, cap.getCurrentTextureName())
ModelPreviewRenderer.submitTexturePreview(g, previewLeft, previewTop, previewRight, previewBottom, cx, cy, zoom, pitch, yaw, modelHolder, showGround, partialTick) })
    }
    open fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        var mouseX: Double = event.x()
        var mouseY: Double = event.y()
        var button: Int = event.button()
        if (button == 0) {
            for (btn in icons) {
                if (btn.contains(mouseX, mouseY)) {
                    btn.onPress.run()
                    return true
                }
            }
        }
        if (isInPreview(mouseX, mouseY)) {
            draggingPreview = true
            draggingButton = button
            return true
        }
        return super.mouseClicked(event, doubleClick)
    }
    open fun mouseReleased(event: MouseButtonEvent): Boolean {
        if (draggingPreview && event.button() == draggingButton) {
            draggingPreview = false
            draggingButton = -1
            return true
        }
        return super.mouseReleased(event)
    }
    open fun mouseDragged(event: MouseButtonEvent, dragX: Double, dragY: Double): Boolean {
        var button: Int = event.button()
        if (draggingPreview && button == draggingButton) {
            if (button == 0) {
                yaw = (yaw + dragX * 1.2 as Float)
                pitch = Mth.clamp((pitch - dragY * 0.8 as Float), -90.0f, 90.0f)
            } else {
                if (button == 1) {
                    offsetX = (offsetX + dragX as Float)
                    offsetY = (offsetY + dragY as Float)
                }
            }
            return true
        }
        return super.mouseDragged(event, dragX, dragY)
    }
    open fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        var delta: Double = scrollY
        if (isInPreview(mouseX, mouseY)) {
            zoom = Mth.clamp((zoom * 1.0 + delta * 0.1 as Float), 18.0f, 360.0f)
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }
    open fun isInPreview(mouseX: Double, mouseY: Double): Boolean {
        return mouseX >= previewLeft && mouseX < previewRight && mouseY >= previewTop && mouseY < previewBottom
    }
    companion object {
        @JvmField var ICON_TEXTURE: Identifier = NameSpaces.MOD.path("texture/icon.png")
        @JvmField var CATEGORY_ORDER: MutableList<String> = List.of("_textures", "main", "extra", "arm", "fp_arm", "tac", "carryon", "parcool", "swem", "slashblade", "tlm", "immersive_melodies", "irons_spell_books", "arrow")
        @JvmStatic fun addFooterRect(out: MutableList<IntArray>, btn: FooterButton) {
            if (btn == null || !btn.visible) {
                return
            }
            out.add(intArrayOf(btn.getX(), btn.getY(), btn.getWidth(), btn.getHeight()))
        }
    }
}