package com.elfmcys.yesstevemodel.client.gui

import com.elfmcys.yesstevemodel.config.GeneralConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Checkbox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class DisclaimerScreen : Screen(Component.literal("Disclaimer GUI")) {
    private var checkbox: Checkbox? = null
    private var textY = 0
    private var textHeight = 0

    override fun init() {
        clearWidgets()
        val size = font.split(Component.translatable("gui.yes_steve_model.disclaimer.text"), 400).size
        val totalHeight = (size * 9) + 20 + 20 + 10 + 20
        textY = (width - 400) / 2
        textHeight = (height - totalHeight) / 2
        val readText = Component.translatable("gui.yes_steve_model.disclaimer.read")
        val textWidth = font.width(readText)

        val cb = Checkbox.builder(readText, font)
            .pos((width - textWidth) / 2, (textHeight + totalHeight) - 50)
            .selected(!GeneralConfig.DISCLAIMER_SHOW.get())
            .build()
        checkbox = cb
        addRenderableWidget(cb)

        val closeButton = Button.builder(Component.translatable("gui.yes_steve_model.disclaimer.close")) {
            if (cb.selected()) {
                GeneralConfig.DISCLAIMER_SHOW.set(false)
                minecraft?.setScreen(PlayerModelScreen())
            } else {
                minecraft?.setScreen(null)
            }
        }.bounds((width - 300) / 2, (textHeight + totalHeight) - 20, 300, 20).build()
        addRenderableWidget(closeButton)
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick)
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        guiGraphics.drawWordWrap(font, Component.translatable("gui.yes_steve_model.disclaimer.text"), textY, textHeight, 400, -1)
    }

    override fun renderBlurredBackground(guiGraphics: GuiGraphics) {
    }
}