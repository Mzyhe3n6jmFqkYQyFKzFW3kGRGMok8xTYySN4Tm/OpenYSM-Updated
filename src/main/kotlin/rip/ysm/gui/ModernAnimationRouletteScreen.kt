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
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.tuple.MutablePair
import org.apache.commons.lang3.tuple.Pair
import org.lwjgl.opengl.GL11
import rip.ysm.api.client.KeyMappingFactory
import rip.ysm.gpu.BlurStack
import rip.ysm.gpu.Pie
import java.util.LinkedList
import java.util.List
import java.util.Map

open class ModernAnimationRouletteScreen : Screen() {
    var centerX: Int = 0
    var centerY: Int = 0
    var hoveredIndex: Int = -1
    var hoveredGearIndex: Int = -1
    var hoveredPathSegment: Int = -1
    var hoveredPrev: Boolean = false
    var hoveredNext: Boolean = false
    var currentNavEntry: Pair<String, Integer> = null
    var currentProperties: OrderedStringMap<String, String> = null
    var renderGroups: MutableMap<String, ExtraAnimationButtons> = null
    var textProperties: MutableMap<String, OrderedStringMap<String, String>> = null
    var animatableModel: AnimatableEntity<*> = null
    var renderContext: ModelAssembly = null
    constructor(modelId: String, modelAssembly: ModelAssembly, animatable: AnimatableEntity<*>) {
        super(Component.literal("YSM Roulette"))
        this.renderContext = modelAssembly
        this.animatableModel = animatable
        this.textProperties = modelAssembly.getModelData().getModelProperties().getExtraAnimationClassify()
        this.renderGroups = modelAssembly.getModelData().getModelProperties().getExtraAnimationButtons()
        if (!lastModelId.equals(modelId)) {
            navigationStack.clear()
            lastModelId = modelId
        }
        if (navigationStack.isEmpty()) {
            navigationStack.add(MutablePair.of(StringPool.EMPTY, 0))
        }
        this.currentNavEntry = navigationStack.peekLast()
        if (this.textProperties.containsKey(this.currentNavEntry.getLeft())) {
            this.currentProperties = this.textProperties.get(this.currentNavEntry.getLeft())
        } else {
            this.currentProperties = modelAssembly.getModelData().getModelProperties().getExtraAnimation()
            navigationStack.clear()
            navigationStack.add(MutablePair.of(StringPool.EMPTY, this.currentNavEntry.getRight()))
            this.currentNavEntry = navigationStack.peekLast()
        }
    }
    open fun init() {
        this.centerX = this.width / 2
        this.centerY = this.height / 2
        if (currentNavEntry.getRight() >= pageCount()) {
            currentNavEntry.setValue(0)
        }
    }
    open fun pageCount(): Int {
        return Math.max(1, currentProperties.size() + 7 / 8)
    }
    open fun page(): Int {
        return currentNavEntry.getRight()
    }
    open fun sliceStartOffset(): Float {
        return -Pie.tau / 16.0f
    }
    open fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (GeneralConfig.BLUR_GUI != null && GeneralConfig.BLUR_GUI.get()) {
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
    open fun collectAndFlushBlur(g: GuiGraphics) {
        var sliceSpan: Float = Pie.tau / 8.0f
        var i = 0
        while (i < 8) {
            var absoluteIdx: Int = i + page() * 8
            if (absoluteIdx >= currentProperties.size()) {
                continue
            }
            var start: Float = sliceStartOffset() + i * sliceSpan + 0.02f
            var end: Float = sliceStartOffset() + i + 1 * sliceSpan - 0.02f
            BlurStack.pushBlurPie(centerX, centerY, 22.0f, 100.0f, start, end, 20.0f)
            i++
        }
        if (pageCount() > 1) {
            BlurStack.pushBlurPie(centerX - 128.0f, centerY, 0.0f, 16.0f, 0.0f, Pie.tau, 20.0f)
            BlurStack.pushBlurPie(centerX + 128.0f, centerY, 0.0f, 16.0f, 0.0f, Pie.tau, 20.0f)
        }
        BlurStack.flush(g)
    }
    open fun updateHover(mouseX: Int, mouseY: Int) {
        var dx: Float = mouseX - centerX
        var dy: Float = mouseY - centerY
        var r: Float = (Math.sqrt(dx * dx + dy * dy) as Float)
        var ang: Float = (Math.atan2(dy, dx) as Float)
        if (ang < 0.0f) {
            ang += Pie.tau
        }
        ang = ang - sliceStartOffset() + Pie.tau % Pie.tau
        var idx: Int = Mth.clamp((ang / Pie.tau / 8.0f as Int), 0, 7)
        hoveredIndex = -1
        hoveredGearIndex = -1
        var absoluteIdx: Int = idx + page() * 8
        if (absoluteIdx < currentProperties.size() && r >= 22.0f && r <= 100.0f) {
            var hasGear: Boolean = currentProperties.getValueAt(absoluteIdx).startsWith("#")
            if (hasGear && r <= 46.0f) {
                hoveredGearIndex = absoluteIdx
            } else {
                hoveredIndex = absoluteIdx
            }
        }
        var prevDx: Float = mouseX - centerX - 128.0f
        var nextDx: Float = mouseX - centerX + 128.0f
        var btnDy: Float = mouseY - centerY
        hoveredPrev = page() > 0 && prevDx * prevDx + btnDy * btnDy <= 16.0f * 16.0f
        hoveredNext = page() + 1 * 8 < currentProperties.size() && nextDx * nextDx + btnDy * btnDy <= 16.0f * 16.0f
    }
    open fun renderSlices(g: GuiGraphics) {
        var sliceSpan: Float = Pie.tau / 8.0f
        var i = 0
        while (i < 8) {
            var absoluteIdx: Int = i + page() * 8
            if (absoluteIdx >= currentProperties.size()) {
                drawSlice(g, i, sliceSpan, 22.0f, 100.0f, 0x30000000)
                continue
            }
            var isHover: Boolean = absoluteIdx == hoveredIndex
            var gearHover: Boolean = absoluteIdx == hoveredGearIndex
            var isSubmenu: Boolean = currentProperties.getKeyAt(absoluteIdx).startsWith("#")
            var hasGear: Boolean = currentProperties.getValueAt(absoluteIdx).startsWith("#")
            var mainColor: Int = if (isHover) if (isSubmenu) (0xD0FFCC00).toInt() else (0xB0FFFFFF).toInt() else if (isSubmenu) 0x70552200 else 0x60000000
            if (hasGear) {
                var gearColor: Int = if (gearHover) (0xD0FFCC00).toInt() else (0x80333333).toInt()
                drawSlice(g, i, sliceSpan, 46.0f, 100.0f, mainColor)
                drawSlice(g, i, sliceSpan, 22.0f, 46.0f, gearColor)
                drawSettingsIcon(g, i, sliceSpan, gearHover)
            } else {
                drawSlice(g, i, sliceSpan, 22.0f, 100.0f, mainColor)
            }
            i++
        }
    }
    open fun drawSlice(g: GuiGraphics, sliceIndex: Int, sliceSpan: Float, inner: Float, outer: Float, color: Int) {
        var start: Float = sliceStartOffset() + sliceIndex * sliceSpan + 0.02f
        var end: Float = sliceStartOffset() + sliceIndex + 1 * sliceSpan - 0.02f
        Pie.draw(g, centerX, centerY, inner, outer, start, end, color, 1.0f)
    }
    open fun drawSettingsIcon(g: GuiGraphics, sliceIndex: Int, sliceSpan: Float, hover: Boolean) {
        var mid: Float = sliceStartOffset() + sliceIndex + 0.5f * sliceSpan
        var r: Float = 34.0f
        var ix: Int = centerX + (r * Math.cos(mid) as Int) - 8
        var iy: Int = centerY + (r * Math.sin(mid) as Int) - 8
        GlStateManager._enableBlend()
        GlStateManager._blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA)
        g.blit(RenderPipelines.GUI_TEXTURED, settingsIcon, ix, iy, 0.0f, 0.0f, 16, 16, 32, 32, 32, 32)
        GlStateManager._disableBlend()
    }
    open fun renderLabels(g: GuiGraphics) {
        var sliceSpan: Float = Pie.tau / 8.0f
        var i = 0
        while (i < 8) {
            var absoluteIdx: Int = i + page() * 8
            if (absoluteIdx >= currentProperties.size()) {
                continue
            }
            var midAngle: Float = sliceStartOffset() + i + 0.5f * sliceSpan
            var hasGear: Boolean = currentProperties.getValueAt(absoluteIdx).startsWith("#")
            var isSubmenuLink: Boolean = currentProperties.getKeyAt(absoluteIdx).startsWith("#")
            var labelR: Float = if (hasGear) 46.0f else 22.0f * 0.5f + 50.0f
            var lx: Int = centerX + (labelR * Math.cos(midAngle) as Int)
            var ly: Int = centerY + (labelR * Math.sin(midAngle) as Int)
            var text: String = displayLabel(absoluteIdx)
            if (StringUtils.isBlank(text)) {
                continue
            }
            var comp: MutableComponent = Component.literal(text)
            if (isSubmenuLink) {
                comp = comp.withStyle(ChatFormatting.GOLD)
            }
            var showKey: Boolean = page() == 0 && navigationStack.size() == 1 && absoluteIdx < ExtraAnimationKey.KEY_MAPPINGS.size()
            var wrapWidth: Int = (100.0f - if (hasGear) 46.0f else 22.0f * 0.9f as Int)
            var lines: MutableList<FormattedCharSequence> = this.font.split(comp, wrapWidth)
            var totalH: Int = lines.size() * 9 + if (showKey) 10 else 0
            var lineY: Int = ly - totalH / 2
            for (line in lines) {
                g.drawCenteredString(this.font, line, lx, lineY, (0xFFFFFFFF).toInt())
                lineY += 9
            }
            if (showKey) {
                renderKeyBinding(g, absoluteIdx, lx, lineY + 1)
            }
            i++
        }
    }
    open fun renderKeyBinding(g: GuiGraphics, slot: Int, x: Int, y: Int) {
        if (slot >= ExtraAnimationKey.KEY_MAPPINGS.size()) {
            return
        }
        var km: KeyMapping = ExtraAnimationKey.KEY_MAPPINGS.get(slot)
        var label: MutableComponent = Component.literal("[ ").withStyle(ChatFormatting.YELLOW)
        if (km.isUnbound()) {
            label.append(Component.translatable("key.yes_steve_model.extra_animation.none"))
        } else {
            label.append(km.getTranslatedKeyMessage())
        }
        label.append(" ]")
        g.drawCenteredString(this.font, label, x, y, (0xFFCFB058).toInt())
    }
    open fun displayLabel(absoluteIdx: Int): String {
        var key: String = currentProperties.getKeyAt(absoluteIdx)
        var value: String = currentProperties.getValueAt(absoluteIdx)
        var display: String = value
        if (value.startsWith("#")) {
            var sub: String = value.substring(1)
            if (renderGroups.containsKey(sub)) {
                display = renderGroups.get(sub).getName()
            }
        }
        if (StringUtils.isBlank(display)) {
            display = key
        }
        return ModelMetadataPresenter.getLocalizedModelString(renderContext, "properties.extra_animation.%s".formatted(key), display)
    }
    open fun renderCenter(g: GuiGraphics) {
        if (animatableModel.getEntity() is Player) {
            var tex: Identifier = if (AnimationLockEvent.isLocked()) lockIcon else unlockIcon
            GlStateManager._enableBlend()
            GlStateManager._blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA)
            g.blit(RenderPipelines.GUI_TEXTURED, tex, centerX - 16, centerY - 16, 0.0f, 0.0f, 32, 32, 64, 64, 64, 64)
            GlStateManager._disableBlend()
        } else {
            g.drawCenteredString(this.font, Component.translatable("gui.yes_steve_model.roulette.stop"), centerX, centerY - 4, (0xFFFFFFFF).toInt())
        }
    }
    open fun renderPageButtons(g: GuiGraphics) {
        if (pageCount() <= 1) {
            return
        }
        drawPageButton(g, centerX - 128.0f, centerY, page() > 0, hoveredPrev, "<")
        drawPageButton(g, centerX + 128.0f, centerY, page() + 1 * 8 < currentProperties.size(), hoveredNext, ">")
    }
    open fun drawPageButton(g: GuiGraphics, cx: Float, cy: Float, enabled: Boolean, hover: Boolean, arrow: String) {
        var color: Int = if (!enabled) 0x40000000 else if (hover) (0xD0FFFFFF).toInt() else (0x90000000).toInt()
        Pie.draw(g, cx, cy, 0.0f, 16.0f, 0.0f, Pie.tau, color, 1.0f)
        var textColor: Int = if (enabled) if (hover) (0xFF000000).toInt() else (0xFFFFFFFF).toInt() else 0x60FFFFFF
        g.drawCenteredString(this.font, arrow, (cx as Int), (cy as Int) - 4, textColor)
    }
    open fun renderPathAndPage(g: GuiGraphics, mouseX: Int, mouseY: Int) {
        layoutAndDrawPath(g, mouseX, mouseY)
        var pageStr: String = String.format("%d/%d", page() + 1, pageCount())
        g.drawCenteredString(this.font, Component.literal(pageStr).withStyle(ChatFormatting.AQUA), centerX, centerY + 108, (0xFFFFFFFF).toInt())
    }
    open fun layoutAndDrawPath(g: GuiGraphics, mouseX: Int, mouseY: Int) {
        var pathY: Int = centerY - 118
        var prefix: String = Component.translatable("gui.yes_steve_model.roulette.path.prefix").getString()
        var rootLabel: String = Component.translatable("gui.yes_steve_model.roulette.path.root").getString()
        var prefixW: Int = this.font.width(prefix)
        var sep: Int = this.font.width(" > ")
        var total: Int = prefixW
        var i = 0
        while (i < navigationStack.size()) {
            var s: String = navigationStack.get(i).getLeft()
            total += this.font.width(if (StringUtils.isBlank(s)) rootLabel else s)
            if (i < navigationStack.size() - 1) {
                total += sep
            }
            i++
        }
        var x: Int = centerX - total / 2
        g.drawString(this.font, prefix, x, pathY, (0xFFFFFFFF).toInt(), true)
        x += prefixW
        hoveredPathSegment = -1
        var i = 0
        while (i < navigationStack.size()) {
            var raw: String = navigationStack.get(i).getLeft()
            var s: String = if (StringUtils.isBlank(raw)) rootLabel else raw
            var w: Int = this.font.width(s)
            var isLast: Boolean = i == navigationStack.size() - 1
            var hover: Boolean = mouseX >= x && mouseX < x + w && mouseY >= pathY - 2 && mouseY < pathY + 10
            var color: Int = if (isLast) (0xFFFFCC00).toInt() else if (hover) (0xFFFFFFFF).toInt() else (0xFFAAAAAA).toInt()
            g.drawString(this.font, s, x, pathY, color, true)
            if (hover && !isLast) {
                g.fill(x, pathY + 9, x + w, pathY + 10, color)
                hoveredPathSegment = i
            }
            x += w
            if (i < navigationStack.size() - 1) {
                g.drawString(this.font, " > ", x, pathY, (0xFF888888).toInt(), true)
                x += sep
            }
            i++
        }
    }
    open fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        var mouseX: Double = event.x()
        var mouseY: Double = event.y()
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
        if (hoveredPathSegment >= 0 && hoveredPathSegment < navigationStack.size() - 1) {
            playClick()
            navigateTo(hoveredPathSegment)
            return true
        }
        if (hoveredGearIndex >= 0) {
            playClick()
            var value: String = currentProperties.getValueAt(hoveredGearIndex)
            if (value.startsWith("#")) {
                var sub: String = value.substring(1)
                if (renderGroups.containsKey(sub)) {
                    Minecraft.getInstance().setScreen(ModelSettingsScreen(renderContext, animatableModel, this, sub))
                    return true
                }
            }
        }
        if (hoveredIndex >= 0) {
            playClick()
            var key: String = currentProperties.getKeyAt(hoveredIndex)
            if ("#return".equals(key)) {
                navigateBack()
            } else {
                if (key.startsWith("#")) {
                    navigateToSubmenu(key)
                } else {
                    playAnimation(key)
                }
            }
            return true
        }
        var cdx: Double = mouseX - centerX
        var cdy: Double = mouseY - centerY
        if (cdx * cdx + cdy * cdy <= 22.0 * 22.0) {
            if (animatableModel.getEntity() is Player) {
                AnimationLockEvent.toggleLock()
            } else {
                NetworkHandler.sendToServer(C2SPlayAnimationPacket.createWithIndex(animatableModel.getEntity().getId()))
                onClose()
            }
            return true
        }
        return super.mouseClicked(event, doubleClick)
    }
    open fun navigateTo(targetIndex: Int) {
        while (navigationStack.size() > targetIndex + 1) {
            navigationStack.removeLast()
        }
        Minecraft.getInstance().setScreen(ModernAnimationRouletteScreen(lastModelId, renderContext, animatableModel))
    }
    open fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (scrollY < 0.0) {
            nextPage()
        } else {
            previousPage()
        }
        return true
    }
    open fun previousPage() {
        currentNavEntry.setValue(Math.max(0, page() - 1))
    }
    open fun nextPage() {
        if (page() + 1 * 8 < currentProperties.size()) {
            currentNavEntry.setValue(page() + 1)
        }
    }
    open fun keyPressed(event: KeyEvent): Boolean {
        if (KeyMappingFactory.isActiveAndMatches(AnimationRouletteKey.KEY_ROULETTE, event)) {
            onClose()
            return true
        }
        return super.keyPressed(event)
    }
    open fun navigateToSubmenu(value: String) {
        if (navigationStack.size() > 5) {
            var p: LocalPlayer = Minecraft.getInstance().player
            if (p != null) {
                p.displayClientMessage(Component.translatable("gui.yes_steve_model.roulette.too_long"), false)
            }
            return
        }
        var sub: String = value.substring(1)
        if (textProperties.get(sub) != null) {
            navigationStack.addLast(MutablePair.of(sub, 0))
            Minecraft.getInstance().setScreen(ModernAnimationRouletteScreen(lastModelId, renderContext, animatableModel))
        }
    }
    open fun navigateBack() {
        if (navigationStack.size() > 1) {
            navigationStack.removeLast()
            Minecraft.getInstance().setScreen(ModernAnimationRouletteScreen(lastModelId, renderContext, animatableModel))
            return
        }
        Minecraft.getInstance().setScreen(null)
    }
    open fun playAnimation(key: String) {
        var player: LocalPlayer = Minecraft.getInstance().player
        if (NetworkHandler.isClientConnected()) {
            var last: Pair<String, Integer> = navigationStack.peekLast()
            var submenu: String = if (last != null && StringUtils.isNotBlank(last.getLeft())) last.getLeft() else StringPool.EMPTY
            var entity: Entity = animatableModel.getEntity()
            if (entity is Player) {
                NetworkHandler.sendToServer(C2SPlayAnimationPacket(hoveredIndex, submenu))
            } else {
                NetworkHandler.sendToServer(C2SPlayAnimationPacket(hoveredIndex, submenu, entity.getId()))
            }
        } else {
            if (player != null) {
                PlayerCapability.get(player).ifPresent({ cap -> cap.requestModelSwitch(key) })
            }
        }
        if (player != null && GeneralConfig.PRINT_ANIMATION_ROULETTE_MSG.get()) {
            player.displayClientMessage(Component.translatable("message.yes_steve_model.model.animation_roulette.play", key), false)
        }
        Minecraft.getInstance().setScreen(null)
    }
    open fun playClick() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f))
    }
    open fun isPauseScreen(): Boolean {
        return false
    }
    companion object {
        @JvmField var settingsIcon: Identifier = NameSpaces.MOD.path("texture/settings.png")
        @JvmField var lockIcon: Identifier = NameSpaces.MOD.path("texture/lock.png")
        @JvmField var unlockIcon: Identifier = NameSpaces.MOD.path("texture/unlock.png")
        @JvmField var navigationStack: LinkedList<Pair<String, Integer>> = Lists.newLinkedList()
        @JvmField var lastModelId: String = StringPool.EMPTY
    }
}