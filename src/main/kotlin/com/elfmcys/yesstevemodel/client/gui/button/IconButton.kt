package com.elfmcys.yesstevemodel.client.gui.button

import com.elfmcys.yesstevemodel.NameSpaces
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier

open class IconButton(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val iconU: Int,
    private val iconV: Int,
    onPress: OnPress
) : FlatColorButton(x, y, width, height, Component.empty(), onPress) {

    override fun renderContents(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.renderContents(guiGraphics, mouseX, mouseY, partialTick)
        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED,
            ICON_TEXTURE,
            x + ((width - 16) / 2),
            y + ((height - 16) / 2),
            iconU.toFloat(),
            iconV.toFloat(),
            16,
            16,
            256,
            256
        )
    }

    companion object {
        val ICON_TEXTURE: Identifier = NameSpaces.MOD.path("texture/icon.png")
    }
}