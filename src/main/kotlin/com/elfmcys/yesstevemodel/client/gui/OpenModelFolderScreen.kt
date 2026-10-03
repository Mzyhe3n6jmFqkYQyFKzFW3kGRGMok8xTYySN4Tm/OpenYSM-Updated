package com.elfmcys.yesstevemodel.client.gui

import com.elfmcys.yesstevemodel.model.ServerModelManager
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.util.Util

class OpenModelFolderScreen(private val parentScreen: PlayerModelScreen) :
    Screen(Component.literal("Open Model Folder")) {

    override fun init() {
        val x = (width - 310) / 2
        val y = (height / 2) + 60
        clearWidgets()
        addRenderableWidget(
            Button.builder(Component.translatable("gui.yes_steve_model.open_model_folder.open")) {
                Util.getPlatform().openFile(ServerModelManager.CUSTOM.toFile())
            }.bounds(x, y, 150, 20).build()
        )
        addRenderableWidget(
            Button.builder(Component.translatable("gui.yes_steve_model.model.return")) {
                minecraft.setScreen(parentScreen)
            }.bounds(x + 160, y, 150, 20).build()
        )
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick)
        guiGraphics.drawWordWrap(
            font,
            Component.translatable("gui.yes_steve_model.open_model_folder.tips"),
            (width - 400) / 2,
            (height / 2) - 80,
            400,
            -1
        )
        super.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    override fun renderBlurredBackground(guiGraphics: GuiGraphics) {
    }
}