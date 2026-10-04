package com.elfmcys.yesstevemodel.client.gui.button

import com.elfmcys.yesstevemodel.config.LoadingStateConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.input.InputWithModifiers
import net.minecraft.network.chat.Component

class LoadingStateButton(x: Int, y: Int) : Button.Plain(x, y, 100, 20, Component.empty(), {}, DEFAULT_NARRATION) {

    override fun renderContents(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.renderContents(guiGraphics, mouseX, mouseY, partialTick)
        guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("gui.yes_steve_model.config.loading_state_position"), x + 105, y + 6, -1, false)
    }

    override fun getMessage(): Component {
        return Component.literal(LoadingStateConfig.LOADING_STATE_POSITION.get().name)
    }

    override fun onPress(input: InputWithModifiers) {
        val nextPosition = when (LoadingStateConfig.LOADING_STATE_POSITION.get()) {
            LoadingStateConfig.Position.TOP_LEFT -> LoadingStateConfig.Position.TOP_CENTER
            LoadingStateConfig.Position.TOP_CENTER -> LoadingStateConfig.Position.TOP_RIGHT
            LoadingStateConfig.Position.TOP_RIGHT -> LoadingStateConfig.Position.BOTTOM_RIGHT
            LoadingStateConfig.Position.BOTTOM_RIGHT -> LoadingStateConfig.Position.BOTTOM_CENTER
            LoadingStateConfig.Position.BOTTOM_CENTER -> LoadingStateConfig.Position.BOTTOM_LEFT
            LoadingStateConfig.Position.BOTTOM_LEFT -> LoadingStateConfig.Position.TOP_LEFT
            else -> LoadingStateConfig.Position.TOP_LEFT
        }
        LoadingStateConfig.LOADING_STATE_POSITION.set(nextPosition)
        LoadingStateConfig.save()
    }
}