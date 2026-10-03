package rip.ysm.gui

import com.elfmcys.yesstevemodel.NameSpaces
import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.client.event.AnimationLockEvent
import com.elfmcys.yesstevemodel.client.gui.ModelMetadataPresenter
import com.elfmcys.yesstevemodel.client.gui.custom.ExtraAnimationButtons
import com.elfmcys.yesstevemodel.client.input.AnimationRouletteKey
import com.elfmcys.yesstevemodel.client.input.ExtraAnimationKey
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.config.GeneralConfig
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.network.message.C2SPlayAnimationPacket
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap
import com.google.common.collect.Lists
import com.mojang.blaze3d.opengl.GlStateManager
import net.minecraft.ChatFormatting
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.FormattedCharSequence
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import org.apache.commons.lang3.tuple.MutablePair
import org.lwjgl.opengl.GL11
import rip.ysm.api.client.KeyMappingFactory
import rip.ysm.gpu.BlurStack
import rip.ysm.gpu.Pie
import java.util.*
import kotlin.math.*

open class ModernAnimationRouletteScreen(
    modelId: String,
    private val renderContext: ModelAssembly,
    private val animatableModel: AnimatableEntity<*>
) : Screen(Component.literal("YSM Roulette")) {

    private var centerX: Int = 0
    private var centerY: Int = 0
    private var hoveredIndex: Int = -1
    private var hoveredGearIndex: Int = -1
    private var hoveredPathSegment: Int = -1
    private var hoveredPrev: Boolean = false
    private var hoveredNext: Boolean = false
    private var currentNavEntry: MutablePair<String, Int>
    private val currentProperties: OrderedStringMap<String, String>
    private val renderGroups: Map<String, ExtraAnimationButtons>
    private val textProperties: Map<String, OrderedStringMap<String, String>>

    init {
        this.textProperties = renderContext.modelData.modelProperties.extraAnimationClassify
        this.renderGroups = renderContext.modelData.modelProperties.extraAnimationButtons
        if (lastModelId != modelId) {
            navigationStack.clear()
            lastModelId = modelId
        }
        if (navigationStack.isEmpty()) {
            navigationStack.add(MutablePair.of(StringPool.EMPTY, 0))
        }
        this.currentNavEntry = navigationStack.peekLast()
        val navKey = this.currentNavEntry.left
        if (navKey != null && this.textProperties.containsKey(navKey)) {
            this.currentProperties =
                this.textProperties[navKey] ?: renderContext.modelData.modelProperties.extraAnimation
        } else {
            this.currentProperties = renderContext.modelData.modelProperties.extraAnimation
            navigationStack.clear()
            navigationStack.add(MutablePair.of(StringPool.EMPTY, this.currentNavEntry.right))
            this.currentNavEntry = navigationStack.peekLast()
        }
    }

    override fun init() {
        this.centerX = this.width / 2
        this.centerY = this.height / 2
        if (currentNavEntry.right >= pageCount()) {
            currentNavEntry.setValue(0)
        }
    }

    private fun pageCount(): Int = max(1, (currentProperties.size + 7) / 8)

    private fun page(): Int = currentNavEntry.right

    private fun sliceStartOffset(): Float = -Pie.tau / 16.0f

    override fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (GeneralConfig.BLUR_GUI.get() == true) {
            collectAndFlushBlur(g)
        }
        updateHover(mouseX, mouseY)
        renderSlices(g)
        renderLabels(g)
        renderCenter(g)
        renderPageButtons(g)
        renderPathAndPage(g, mouseX, mouseY)
        super.render(g, mouseX, mouseY, partialTick)
    }

    private fun collectAndFlushBlur(g: GuiGraphics) {
        val sliceSpan: Float = Pie.tau / 8.0f
        for (i in 0 until 8) {
            val absoluteIdx: Int = i + page() * 8
            if (absoluteIdx >= currentProperties.size) {
                continue
            }
            val start: Float = sliceStartOffset() + i * sliceSpan + 0.02f
            val end: Float = sliceStartOffset() + (i + 1) * sliceSpan - 0.02f
            BlurStack.pushBlurPie(centerX.toFloat(), centerY.toFloat(), 22.0f, 100.0f, start, end, 20.0f)
        }
        if (pageCount() > 1) {
            BlurStack.pushBlurPie(centerX - 128.0f, centerY.toFloat(), 0.0f, 16.0f, 0.0f, Pie.tau, 20.0f)
            BlurStack.pushBlurPie(centerX + 128.0f, centerY.toFloat(), 0.0f, 16.0f, 0.0f, Pie.tau, 20.0f)
        }
        BlurStack.flush(g)
    }

    private fun updateHover(mouseX: Int, mouseY: Int) {
        val dx: Float = (mouseX - centerX).toFloat()
        val dy: Float = (mouseY - centerY).toFloat()
        val r: Float = sqrt(dx * dx + dy * dy)
        var ang: Float = atan2(dy, dx)
        if (ang < 0.0f) {
            ang += Pie.tau
        }
        ang = (ang - sliceStartOffset() + Pie.tau) % Pie.tau
        val idx: Int = Mth.clamp((ang / (Pie.tau / 8.0f)).toInt(), 0, 7)
        hoveredIndex = -1
        hoveredGearIndex = -1
        val absoluteIdx: Int = idx + page() * 8
        if (absoluteIdx < currentProperties.size && r in 22.0f..100.0f) {
            val hasGear: Boolean = currentProperties.getValueAt(absoluteIdx).startsWith("#")
            if (hasGear && r <= 46.0f) {
                hoveredGearIndex = absoluteIdx
            } else {
                hoveredIndex = absoluteIdx
            }
        }
        val prevDx: Float = (mouseX - (centerX - 128)).toFloat()
        val nextDx: Float = (mouseX - (centerX + 128)).toFloat()
        val btnDy: Float = (mouseY - centerY).toFloat()
        hoveredPrev = page() > 0 && (prevDx * prevDx + btnDy * btnDy <= 16.0f * 16.0f)
        hoveredNext = (page() + 1) * 8 < currentProperties.size && (nextDx * nextDx + btnDy * btnDy <= 16.0f * 16.0f)
    }

    private fun renderSlices(g: GuiGraphics) {
        val sliceSpan: Float = Pie.tau / 8.0f
        for (i in 0 until 8) {
            val absoluteIdx: Int = i + page() * 8
            if (absoluteIdx >= currentProperties.size) {
                drawSlice(g, i, sliceSpan, 22.0f, 100.0f, 0x30000000)
                continue
            }
            val isHover: Boolean = absoluteIdx == hoveredIndex
            val gearHover: Boolean = absoluteIdx == hoveredGearIndex
            val isSubmenu: Boolean = currentProperties.getKeyAt(absoluteIdx).startsWith("#")
            val hasGear: Boolean = currentProperties.getValueAt(absoluteIdx).startsWith("#")
            val mainColor: Int =
                if (isHover) (if (isSubmenu) 0xD0FFCC00.toInt() else 0xB0FFFFFF.toInt()) else (if (isSubmenu) 0x70552200.toInt() else 0x60000000)
            if (hasGear) {
                val gearColor: Int = if (gearHover) 0xD0FFCC00.toInt() else 0x80333333.toInt()
                drawSlice(g, i, sliceSpan, 46.0f, 100.0f, mainColor)
                drawSlice(g, i, sliceSpan, 22.0f, 46.0f, gearColor)
                drawSettingsIcon(g, i, sliceSpan, gearHover)
            } else {
                drawSlice(g, i, sliceSpan, 22.0f, 100.0f, mainColor)
            }
        }
    }

    private fun drawSlice(g: GuiGraphics, sliceIndex: Int, sliceSpan: Float, inner: Float, outer: Float, color: Int) {
        val start: Float = sliceStartOffset() + sliceIndex * sliceSpan + 0.02f
        val end: Float = sliceStartOffset() + (sliceIndex + 1) * sliceSpan - 0.02f
        Pie.draw(g, centerX.toFloat(), centerY.toFloat(), inner, outer, start, end, color, 1.0f)
    }

    private fun drawSettingsIcon(g: GuiGraphics, sliceIndex: Int, sliceSpan: Float, hover: Boolean) {
        val mid: Float = sliceStartOffset() + (sliceIndex + 0.5f) * sliceSpan
        val r = 34.0f
        val ix: Int = centerX + (r * cos(mid)).toInt() - 8
        val iy: Int = centerY + (r * sin(mid)).toInt() - 8
        GlStateManager._enableBlend()
        GlStateManager._blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA)
        g.blit(RenderPipelines.GUI_TEXTURED, settingsIcon, ix, iy, 0.0f, 0.0f, 16, 16, 32, 32, 32, 32)
        GlStateManager._disableBlend()
    }

    private fun renderLabels(g: GuiGraphics) {
        val sliceSpan: Float = Pie.tau / 8.0f
        for (i in 0 until 8) {
            val absoluteIdx: Int = i + page() * 8
            if (absoluteIdx >= currentProperties.size) {
                continue
            }
            val midAngle: Float = sliceStartOffset() + (i + 0.5f) * sliceSpan
            val hasGear: Boolean = currentProperties.getValueAt(absoluteIdx).startsWith("#")
            val isSubmenuLink: Boolean = currentProperties.getKeyAt(absoluteIdx).startsWith("#")
            val labelR: Float = (if (hasGear) 46.0f else 22.0f) * 0.5f + 50.0f
            val lx: Int = centerX + (labelR * cos(midAngle)).toInt()
            val ly: Int = centerY + (labelR * sin(midAngle)).toInt()
            val text: String = displayLabel(absoluteIdx)
            if (text.isBlank()) {
                continue
            }
            var comp: MutableComponent = Component.literal(text)
            if (isSubmenuLink) {
                comp = comp.withStyle(ChatFormatting.GOLD)
            }
            val showKey: Boolean =
                page() == 0 && navigationStack.size == 1 && absoluteIdx < ExtraAnimationKey.KEY_MAPPINGS.size
            val wrapWidth: Int = ((100.0f - (if (hasGear) 46.0f else 22.0f)) * 0.9f).toInt()
            val lines: List<FormattedCharSequence> = font.split(comp, wrapWidth)
            val totalH: Int = lines.size * 9 + if (showKey) 10 else 0
            var lineY: Int = ly - totalH / 2
            for (line in lines) {
                g.drawCenteredString(font, line, lx, lineY, 0xFFFFFFFF.toInt())
                lineY += 9
            }
            if (showKey) {
                renderKeyBinding(g, absoluteIdx, lx, lineY + 1)
            }
        }
    }

    private fun renderKeyBinding(g: GuiGraphics, slot: Int, x: Int, y: Int) {
        if (slot >= ExtraAnimationKey.KEY_MAPPINGS.size) {
            return
        }
        val km: KeyMapping = ExtraAnimationKey.KEY_MAPPINGS[slot]
        val label: MutableComponent = Component.literal("[ ").withStyle(ChatFormatting.YELLOW)
        if (km.isUnbound) {
            label.append(Component.translatable("key.yes_steve_model.extra_animation.none"))
        } else {
            label.append(km.translatedKeyMessage)
        }
        label.append(" ]")
        g.drawCenteredString(font, label, x, y, 0xFFCFB058.toInt())
    }

    private fun displayLabel(absoluteIdx: Int): String {
        val key: String = currentProperties.getKeyAt(absoluteIdx)
        val value: String = currentProperties.getValueAt(absoluteIdx)
        var display: String = value
        if (value.startsWith("#")) {
            val sub: String = value.substring(1)
            if (renderGroups.containsKey(sub)) {
                display = renderGroups[sub]?.name ?: sub
            }
        }
        if (display.isBlank()) {
            display = key
        }
        return ModelMetadataPresenter.getLocalizedModelString(renderContext, "properties.extra_animation.$key", display)
    }

    private fun renderCenter(g: GuiGraphics) {
        if (animatableModel.entity is Player) {
            val tex: Identifier = if (AnimationLockEvent.isLocked()) lockIcon else unlockIcon
            GlStateManager._enableBlend()
            GlStateManager._blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA)
            g.blit(RenderPipelines.GUI_TEXTURED, tex, centerX - 16, centerY - 16, 0.0f, 0.0f, 32, 32, 64, 64, 64, 64)
            GlStateManager._disableBlend()
        } else {
            g.drawCenteredString(
                font,
                Component.translatable("gui.yes_steve_model.roulette.stop"),
                centerX,
                centerY - 4,
                0xFFFFFFFF.toInt()
            )
        }
    }

    private fun renderPageButtons(g: GuiGraphics) {
        if (pageCount() <= 1) {
            return
        }
        drawPageButton(g, centerX - 128.0f, centerY.toFloat(), page() > 0, hoveredPrev, "<")
        drawPageButton(
            g,
            centerX + 128.0f,
            centerY.toFloat(),
            (page() + 1) * 8 < currentProperties.size,
            hoveredNext,
            ">"
        )
    }

    private fun drawPageButton(g: GuiGraphics, cx: Float, cy: Float, enabled: Boolean, hover: Boolean, arrow: String) {
        val color: Int = if (!enabled) 0x40000000 else if (hover) 0xD0FFFFFF.toInt() else 0x90000000.toInt()
        Pie.draw(g, cx, cy, 0.0f, 16.0f, 0.0f, Pie.tau, color, 1.0f)
        val textColor: Int = if (enabled) (if (hover) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()) else 0x60FFFFFF
        g.drawCenteredString(font, arrow, cx.toInt(), cy.toInt() - 4, textColor)
    }

    private fun renderPathAndPage(g: GuiGraphics, mouseX: Int, mouseY: Int) {
        layoutAndDrawPath(g, mouseX, mouseY)
        val pageStr: String = "%d/%d".format(page() + 1, pageCount())
        g.drawCenteredString(
            font,
            Component.literal(pageStr).withStyle(ChatFormatting.AQUA),
            centerX,
            centerY + 108,
            0xFFFFFFFF.toInt()
        )
    }

    private fun layoutAndDrawPath(g: GuiGraphics, mouseX: Int, mouseY: Int) {
        val pathY = centerY - 118
        val prefix = Component.translatable("gui.yes_steve_model.roulette.path.prefix").string
        val rootLabel = Component.translatable("gui.yes_steve_model.roulette.path.root").string
        val prefixW = font.width(prefix)
        val sep = font.width(" > ")
        var total = prefixW
        for (i in 0 until navigationStack.size) {
            val s = navigationStack[i].left
            total += font.width(if (s.isNullOrBlank()) rootLabel else s)
            if (i < navigationStack.size - 1) {
                total += sep
            }
        }
        var x = centerX - total / 2
        g.drawString(font, prefix, x, pathY, 0xFFFFFFFF.toInt(), true)
        x += prefixW
        hoveredPathSegment = -1
        for (i in 0 until navigationStack.size) {
            val raw = navigationStack[i].left
            val s = if (raw.isNullOrBlank()) rootLabel else raw
            val w = font.width(s)
            val isLast = i == navigationStack.size - 1
            val hover = mouseX in x until (x + w) && mouseY in (pathY - 2) until (pathY + 10)
            val color = if (isLast) 0xFFFFCC00.toInt() else (if (hover) 0xFFFFFFFF.toInt() else 0xFFAAAAAA.toInt())
            g.drawString(font, s, x, pathY, color, true)
            if (hover && !isLast) {
                g.fill(x, pathY + 9, x + w, pathY + 10, color)
                hoveredPathSegment = i
            }
            x += w
            if (i < navigationStack.size - 1) {
                g.drawString(font, " > ", x, pathY, 0xFF888888.toInt(), true)
                x += sep
            }
        }
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val mouseX = event.x()
        val mouseY = event.y()
        if (hoveredPrev) {
            playClick()
            previousPage()
            return true
        }
        if (hoveredNext) {
            playClick()
            nextPage()
            return true
        }
        if (hoveredPathSegment in 0 until (navigationStack.size - 1)) {
            playClick()
            navigateTo(hoveredPathSegment)
            return true
        }
        if (hoveredGearIndex >= 0) {
            playClick()
            val value = currentProperties.getValueAt(hoveredGearIndex)
            if (value.startsWith("#")) {
                val sub = value.substring(1)
                if (renderGroups.containsKey(sub)) {
                    Minecraft.getInstance().setScreen(ModelSettingsScreen(renderContext, animatableModel, this, sub))
                    return true
                }
            }
        }
        if (hoveredIndex >= 0) {
            playClick()
            val key = currentProperties.getKeyAt(hoveredIndex)
            if ("#return" == key) {
                navigateBack()
            } else if (key.startsWith("#")) {
                navigateToSubmenu(key)
            } else {
                playAnimation(key)
            }
            return true
        }
        val cdx = mouseX - centerX
        val cdy = mouseY - centerY
        if (cdx * cdx + cdy * cdy <= 22.0 * 22.0) {
            if (animatableModel.entity is Player) {
                AnimationLockEvent.toggleLock()
            } else {
                NetworkHandler.sendToServer(C2SPlayAnimationPacket.createWithIndex(animatableModel.entity.id))
                onClose()
            }
            return true
        }
        return super.mouseClicked(event, doubleClick)
    }

    private fun navigateTo(targetIndex: Int) {
        while (navigationStack.size > targetIndex + 1) {
            navigationStack.removeLast()
        }
        Minecraft.getInstance().setScreen(ModernAnimationRouletteScreen(lastModelId, renderContext, animatableModel))
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (scrollY < 0.0) {
            nextPage()
        } else {
            previousPage()
        }
        return true
    }

    private fun previousPage() {
        currentNavEntry.setValue(max(0, page() - 1))
    }

    private fun nextPage() {
        if ((page() + 1) * 8 < currentProperties.size) {
            currentNavEntry.setValue(page() + 1)
        }
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (KeyMappingFactory.isActiveAndMatches(AnimationRouletteKey.KEY_ROULETTE, event)) {
            onClose()
            return true
        }
        return super.keyPressed(event)
    }

    private fun navigateToSubmenu(value: String) {
        if (navigationStack.size > 5) {
            val p = Minecraft.getInstance().player
            p?.displayClientMessage(Component.translatable("gui.yes_steve_model.roulette.too_long"), false)
            return
        }
        val sub = value.substring(1)
        if (textProperties[sub] != null) {
            navigationStack.addLast(MutablePair.of(sub, 0))
            Minecraft.getInstance().setScreen(ModernAnimationRouletteScreen(lastModelId, renderContext, animatableModel))
        }
    }

    private fun navigateBack() {
        if (navigationStack.size > 1) {
            navigationStack.removeLast()
            Minecraft.getInstance().setScreen(ModernAnimationRouletteScreen(lastModelId, renderContext, animatableModel))
            return
        }
        Minecraft.getInstance().setScreen(null)
    }

    private fun playAnimation(key: String) {
        val player: LocalPlayer? = Minecraft.getInstance().player
        if (NetworkHandler.isClientConnected()) {
            val last = navigationStack.peekLast()
            val submenu = if (last != null && !last.left.isNullOrBlank()) last.left else StringPool.EMPTY
            val entity: Entity = animatableModel.entity
            if (entity is Player) {
                NetworkHandler.sendToServer(C2SPlayAnimationPacket(hoveredIndex, submenu))
            } else {
                NetworkHandler.sendToServer(C2SPlayAnimationPacket(hoveredIndex, submenu, entity.id))
            }
        } else if (player != null) {
            PlayerCapability[player]?.requestModelSwitch(key)
        }
        if (player != null && GeneralConfig.PRINT_ANIMATION_ROULETTE_MSG.get() == true) {
            player.displayClientMessage(Component.translatable("message.yes_steve_model.model.animation_roulette.play", key), false)
        }
        Minecraft.getInstance().setScreen(null)
    }

    private fun playClick() {
        Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f))
    }

    override fun isPauseScreen(): Boolean = false

    companion object {
        val settingsIcon: Identifier = NameSpaces.MOD.path("texture/settings.png")
        val lockIcon: Identifier = NameSpaces.MOD.path("texture/lock.png")
        val unlockIcon: Identifier = NameSpaces.MOD.path("texture/unlock.png")
        val navigationStack: LinkedList<MutablePair<String, Int>> = Lists.newLinkedList()
        var lastModelId: String = StringPool.EMPTY
    }
}