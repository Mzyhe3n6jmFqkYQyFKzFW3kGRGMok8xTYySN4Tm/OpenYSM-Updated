package com.elfmcys.yesstevemodel.client.gui

import com.elfmcys.yesstevemodel.client.gui.button.FlatColorButton
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class DownloadScreen(private val parentScreen: PlayerModelScreen) : Screen(Component.literal("YSM Config GUI")) {
    private var guiLeft = 0
    private var guiTop = 0

    override fun init() {
        guiLeft = (width - 420) / 2
        guiTop = (height - 235) / 2
        addRenderableWidget(
            FlatColorButton(
                guiLeft + 5,
                guiTop,
                80,
                18,
                Component.translatable("gui.yes_steve_model.model.return")
            ) {
                minecraft.setScreen(parentScreen)
            })
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick)
        val color = ChatFormatting.DARK_RED.color ?: 0xAA0000
        guiGraphics.drawCenteredString(
            font,
            "Coming Soooooooooooooooooooooooooon™",
            width / 2,
            (height / 2) - 5,
            color or 0xFF000000.toInt()
        )
        super.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    override fun renderBlurredBackground(guiGraphics: GuiGraphics) {
    }
}