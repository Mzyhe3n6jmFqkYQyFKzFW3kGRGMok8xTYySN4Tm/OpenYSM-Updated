package com.elfmcys.yesstevemodel.client.gui.button

import com.elfmcys.yesstevemodel.NameSpaces
import com.elfmcys.yesstevemodel.client.gui.ISpecialWidget
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractButton
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.input.InputWithModifiers
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier

@Environment(EnvType.CLIENT)
open class ConfigCheckBox(
    x: Int,
    y: Int,
    width: Int = 115,
    private val component2: Component,
    private val consumer2: (Boolean) -> Unit
) : AbstractButton(x, y, width, 12, component2), ISpecialWidget {
    constructor(x: Int, y: Int, component: Component, consumer: (Boolean) -> Unit) : this(
        x,
        y,
        component2 = component,
        consumer2 = consumer
    )

    var isStateTriggered: Boolean = false

    override fun renderContents(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val u = if (isStateTriggered) 128.0f else 0.0f
        val v = if (isHovered) 12.0f else 0.0f
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, u, v, width, height, 256, 256)
        guiGraphics.drawString(Minecraft.getInstance().font, component2, x + 14, y + 2, -1, false)
    }

    override fun onPress(input: InputWithModifiers) {
        isStateTriggered = !isStateTriggered
        consumer2(isStateTriggered)
    }

    override fun updateWidgetNarration(output: NarrationElementOutput) {
        defaultButtonNarrationText(output)
    }

    companion object {
        val TEXTURE: Identifier = NameSpaces.MOD.path("texture/roulette.png")
    }
}