package com.elfmcys.yesstevemodel.client.gui.button

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

open class FlatColorButton(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    component: Component,
    onPress: OnPress
) : Button(x, y, width, height, component, onPress, DEFAULT_NARRATION) {

    var selected: Boolean = false
    private var tooltip: List<Component>? = null

    fun setTooltipText(str: String): FlatColorButton {
        this.tooltip = listOf(Component.translatable(str))
        return this
    }

    fun setTooltipLines(list: List<Component>): FlatColorButton {
        this.tooltip = list
        return this
    }

    fun renderTooltip(guiGraphics: GuiGraphics, screen: Screen, mouseX: Int, mouseY: Int) {
        val tips = tooltip
        if (isHovered && tips != null) {
            guiGraphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, tips, mouseX, mouseY)
        }
    }

    override fun renderContents(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (selected) {
            guiGraphics.fillGradient(x, y, x + width, y + height, -14774017, -14774017)
        } else {
            guiGraphics.fillGradient(x, y, x + width, y + height, -12369342, -12369342)
        }
        if (isHoveredOrFocused) {
            guiGraphics.fillGradient(x, y + 1, x + 1, (y + height) - 1, -790560, -790560)
            guiGraphics.fillGradient(x, y, x + width, y + 1, -790560, -790560)
            guiGraphics.fillGradient((x + width) - 1, y + 1, x + width, (y + height) - 1, -790560, -790560)
            guiGraphics.fillGradient(x, (y + height) - 1, x + width, y + height, -790560, -790560)
        }
        renderScrollingStringOverContents(guiGraphics.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.NONE), message, 2)
    }
}