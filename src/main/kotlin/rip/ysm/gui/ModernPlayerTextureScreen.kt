package rip.ysm.gui

import com.elfmcys.yesstevemodel.NameSpaces
import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.client.entity.PlayerPreviewEntity
import com.elfmcys.yesstevemodel.client.gui.PlayerModelScreen
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap
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
import rip.ysm.gui.components.buttons.IconButton
import rip.ysm.gui.components.groups.CategoryGroup
import rip.ysm.gui.components.groups.TextureGroup
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

open class ModernPlayerTextureScreen(
    parent: PlayerModelScreen,
    val modelId: String,
    val renderContext: ModelAssembly
) : OptionScreen(Component.translatable("gui.yes_steve_model.texture_screen.title"), parent) {
    val textureMap: OrderedStringMap<String, out AbstractTexture> = renderContext.animationBundle.textures
    private val modelHolder: PlayerPreviewEntity = PlayerPreviewEntity()
    private val icons: MutableList<IconButton> = ArrayList()
    private var hoveredIcon: IconButton? = null
    private var searchBox: EditBox? = null
    var currentAnimation: String = StringPool.EMPTY

    private var previewLeft: Int = 0
    private var previewTop: Int = 0
    private var previewRight: Int = 0
    private var previewBottom: Int = 0

    private var yaw: Float = 165.0f
    private var pitch: Float = -5.0f
    private var zoom: Float = 80.0f
    private var offsetX: Float = 0.0f
    private var offsetY: Float = -60.0f
    private var showGround: Boolean = true

    private var draggingPreview: Boolean = false
    private var draggingButton: Int = -1

    override fun computePanelWidth(): Int = min(width - 40, 780)

    override fun computePanelHeight(): Int = min(height - 40, 380)

    override fun shouldUseCompactTabs(): Boolean = width < 640

    override fun computeRowAreaRight(): Int = panelRight - previewWidth() - 6

    private fun previewWidth(): Int = if (compactTabs) 200 else 280

    override fun registerGroups() {
        if (!textureMap.isEmpty()) {
            val tg = TextureGroup()
            tg.add(TextureGrid(this))
            groups.add(tg)
        }
        val mainAnims = renderContext.animationBundle.mainAnimations
        val buckets: MutableMap<String, MutableList<String>> = LinkedHashMap()
        for ((name, anim) in mainAnims) {
            if (name.startsWith("——")) continue
            val key = anim.sourceKey ?: "misc"
            buckets.computeIfAbsent(key) { ArrayList() }.add(name)
        }
        val sortedCats: MutableList<String> = ArrayList(buckets.keys)
        sortedCats.sortWith { a, b ->
            var ia = CATEGORY_ORDER.indexOf(a)
            var ib = CATEGORY_ORDER.indexOf(b)
            if (ia < 0) ia = Int.MAX_VALUE
            if (ib < 0) ib = Int.MAX_VALUE
            if (ia != ib) ia.compareTo(ib) else a.compareTo(b)
        }
        for (cat in sortedCats) {
            val g = CategoryGroup(cat)
            val names = buckets[cat] ?: continue
            for (name in names) {
                g.add(AnimationRow(0, 0, 0, 18, name, this))
            }
            groups.add(g)
        }
    }

    override fun init() {
        super.init()
        applyBtn.let { removeWidget(it); it.visible = false; it.active = false }
        undoBtn.let { removeWidget(it); it.visible = false; it.active = false }
        cancelBtn.let { removeWidget(it); it.visible = false }
        saveBtn.let {
            it.message = Component.translatable("gui.yes_steve_model.config.done")
            it.x = panelRight - it.width
        }

        previewLeft = panelRight - previewWidth()
        previewTop = rowAreaTop
        previewRight = panelRight
        previewBottom = panelBottom - 60

        icons.clear()
        val iconY = panelTop
        var iconX = panelRight - 18
        icons.add(
            IconButton(
                iconX,
                iconY,
                18,
                64,
                16,
                { currentAnimation = "idle" },
                Component.translatable("gui.yes_steve_model.model.stop")
            )
        )
        iconX -= 20
        icons.add(
            IconButton(
                iconX,
                iconY,
                18,
                48,
                16,
                ::resetView,
                Component.translatable("gui.yes_steve_model.model.reset")
            )
        )
        iconX -= 20
        icons.add(
            IconButton(
                iconX,
                iconY,
                18,
                64,
                0,
                { showGround = !showGround },
                Component.translatable("gui.yes_steve_model.model.ground")
            )
        )

        val searchW = Mth.clamp(panelRight - panelLeft - 3 * 18 - 2 * 2 - 200, 80, 140)
        val searchX = iconX - 2 - searchW
        val oldQuery = searchBox?.value ?: ""
        val box =
            EditBox(font, searchX, iconY, searchW, 18, Component.translatable("gui.yes_steve_model.search.placeholder"))
        box.setTextColor(0xFFFFFF)
        box.setHint(Component.translatable("gui.yes_steve_model.search.placeholder"))
        box.setMaxLength(64)
        box.value = oldQuery
        box.setResponder { applySearchFilter() }
        searchBox = box
        addRenderableWidget(box)
    }

    override fun selectGroup(group: OptionGroup) {
        super.selectGroup(group)
        applySearchFilter()
    }

    private fun applySearchFilter() {
        val group = activeGroup ?: return
        val s = searchBox?.value?.lowercase()?.trim() ?: ""
        for (r in activeRows) {
            r.closeOverlay()
        }
        activeRows.clear()
        var rowY = rowAreaTop
        val rowW = rowAreaRight - rowAreaLeft
        for (template in group.rows) {
            if (s.isNotEmpty() && template is AnimationRow && !template.matches(s)) {
                continue
            }
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

    private fun resetView() {
        offsetX = 0.0f
        offsetY = -60.0f
        zoom = 80.0f
        yaw = 165.0f
        pitch = -5.0f
    }

    fun selectAnimation(name: String) {
        currentAnimation = name
        if (!modelHolder.getAnimationStateMachine().isCurrentAnimation(name)) {
            modelHolder.getAnimationStateMachine().setCurrentAnimation(name)
        }
    }

    override fun onClose() {
        minecraft.setScreen(parentScreen)
    }

    override fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        hoveredIcon = null
        for (btn in icons) {
            if (btn.contains(mouseX.toDouble(), mouseY.toDouble())) {
                hoveredIcon = btn
                break
            }
        }
        super.render(g, mouseX, mouseY, partialTick)
        for (btn in icons) {
            drawIcon(g, btn)
        }
    }

    private fun drawIcon(g: GuiGraphics, btn: IconButton) {
        val hover = btn == hoveredIcon
        val bg = if (hover) 0x90171717.toInt() else 0x90000000.toInt()
        g.fill(btn.x, btn.y, btn.x + btn.size, btn.y + btn.size, bg)
        val ix = btn.x + (btn.size - 16) / 2
        val iy = btn.y + (btn.size - 16) / 2
        g.blit(
            RenderPipelines.GUI_TEXTURED,
            ICON_TEXTURE,
            ix,
            iy,
            btn.u.toFloat(),
            btn.v.toFloat(),
            16,
            16,
            16,
            16,
            256,
            256
        )
    }

    override fun collectBlurRegions(out: MutableList<IntArray>) {
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
        if (activeGroup is TextureGroup && activeRows.isNotEmpty() && activeRows[0] is TextureGrid) {
            (activeRows[0] as TextureGrid).collectBlurRegions(
                out,
                rowScrollDisplay.roundToInt(),
                rowAreaTop,
                rowAreaBottom
            )
        } else {
            val rowScroll = rowScrollDisplay.roundToInt()
            for (row in activeRows) {
                val y = row.y - rowScroll
                val yBot = y + row.height
                if (yBot <= rowAreaTop || y >= rowAreaBottom) {
                    continue
                }
                val top = max(y, rowAreaTop)
                val bot = min(yBot, rowAreaBottom)
                out.add(intArrayOf(row.x, top, row.width, bot - top))
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
        val sb = searchBox
        if (sb != null && sb.visible) {
            out.add(intArrayOf(sb.x, sb.y, sb.width, sb.height))
        }
        if (hoveredIcon != null || hoveredRow is AnimationRow) {
            val descY = panelBottom - 32
            out.add(intArrayOf(panelLeft, descY, panelRight - panelLeft, 28))
        }
    }

    override fun renderExtras(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        g.fill(previewLeft, previewTop, previewRight, previewBottom, 0x66000000)
        renderPreview(g, partialTick)
    }

    override fun renderDescription(g: GuiGraphics, descY: Int) {
        val hIcon = hoveredIcon
        if (hIcon != null) {
            g.fill(panelLeft, descY, panelRight, descY + 28, 0x80000000.toInt())
            val tooltip = hIcon.tooltip
            if (tooltip != null) {
                g.drawString(font, tooltip, panelLeft + 6, descY + 10, -1, false)
            }
            return
        }
        val hRow = hoveredRow
        if (hRow is AnimationRow) {
            g.fill(panelLeft, descY, panelRight, descY + 28, 0x80000000.toInt())
            g.drawString(font, hRow.message, panelLeft + 6, descY + 4, -1, false)
            g.drawString(
                font,
                Component.literal(hRow.animKey).withStyle(ChatFormatting.GRAY),
                panelLeft + 6,
                descY + 16,
                0xFFAAAAAA.toInt(),
                false
            )
        }
    }

    private fun renderPreview(g: GuiGraphics, partialTick: Float) {
        val mc = minecraft
        val player = mc.player ?: return
        if (!modelHolder.getAnimationStateMachine().isCurrentAnimation(currentAnimation)) {
            modelHolder.getAnimationStateMachine().setCurrentAnimation(currentAnimation)
        }
        val cap = PlayerCapability[player]
        if (cap != null) {
            modelHolder.initModelWithTexture(modelId, cap.currentTextureName)
            val cx = (previewLeft + previewRight) / 2.0f + offsetX
            val cy = previewTop + (previewBottom - previewTop) * 0.65f + offsetY
            ModelPreviewRenderer.submitTexturePreview(
                g,
                previewLeft,
                previewTop,
                previewRight,
                previewBottom,
                cx,
                cy,
                zoom,
                pitch,
                yaw,
                modelHolder,
                showGround,
                partialTick
            )
        }
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val mouseX = event.x()
        val mouseY = event.y()
        val button = event.button()
        if (button == 0) {
            for (btn in icons) {
                if (btn.contains(mouseX, mouseY)) {
                    btn.onPress()
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

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        if (draggingPreview && event.button() == draggingButton) {
            draggingPreview = false
            draggingButton = -1
            return true
        }
        return super.mouseReleased(event)
    }

    override fun mouseDragged(event: MouseButtonEvent, dx: Double, dy: Double): Boolean {
        val button = event.button()
        if (draggingPreview && button == draggingButton) {
            when (button) {
                0 -> {
                    yaw = (yaw + dx * 1.2).toFloat()
                    pitch = Mth.clamp((pitch - dy * 0.8).toFloat(), -90.0f, 90.0f)
                }

                1 -> {
                    offsetX = (offsetX + dx).toFloat()
                    offsetY = (offsetY + dy).toFloat()
                }
            }
            return true
        }
        return super.mouseDragged(event, dx, dy)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (isInPreview(mouseX, mouseY)) {
            zoom = Mth.clamp((zoom * (1.0 + scrollY * 0.1)).toFloat(), 18.0f, 360.0f)
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    private fun isInPreview(mouseX: Double, mouseY: Double): Boolean =
        mouseX >= previewLeft && mouseX < previewRight && mouseY >= previewTop && mouseY < previewBottom

    companion object {
        val ICON_TEXTURE: Identifier = NameSpaces.MOD.path("texture/icon.png")
        val CATEGORY_ORDER: List<String> = listOf(
            "_textures", "main", "extra", "arm", "fp_arm", "tac", "carryon",
            "parcool", "swem", "slashblade", "tlm", "immersive_melodies",
            "irons_spell_books", "arrow"
        )
    }
}