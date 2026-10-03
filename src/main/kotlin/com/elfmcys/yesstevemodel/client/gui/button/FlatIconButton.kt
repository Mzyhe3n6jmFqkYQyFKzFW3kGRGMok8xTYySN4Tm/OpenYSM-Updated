package com.elfmcys.yesstevemodel.client.gui.button

import com.elfmcys.yesstevemodel.client.gui.ISpecialWidget
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component

@Environment(EnvType.CLIENT)
class FlatIconButton(
    x: Int,
    y: Int,
    private val backgroundHeight: Int,
    component: Component
) : AbstractWidget(x, y, 115, 15, component), ISpecialWidget {

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        guiGraphics.fill(x, y, x + width, y + backgroundHeight, -280804798)
        renderScrollingStringOverContents(guiGraphics.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.NONE), message, 2)
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput)
    }
}