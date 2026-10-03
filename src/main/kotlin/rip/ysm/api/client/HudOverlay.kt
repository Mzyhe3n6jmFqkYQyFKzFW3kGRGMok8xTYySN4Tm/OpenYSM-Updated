package rip.ysm.api.client

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics

fun interface HudOverlay {
    fun render(guiGraphics: GuiGraphics, font: Font, partialTick: Float, screenWidth: Int, screenHeight: Int)
}