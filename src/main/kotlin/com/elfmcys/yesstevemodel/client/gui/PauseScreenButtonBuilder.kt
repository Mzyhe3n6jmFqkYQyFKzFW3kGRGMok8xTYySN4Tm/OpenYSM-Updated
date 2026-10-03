package com.elfmcys.yesstevemodel.client.gui

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.config.GeneralConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.screens.PauseScreen
import net.minecraft.network.chat.Component

object PauseScreenButtonBuilder {
    @JvmStatic
    fun isServerConnected(): Boolean = YesSteveModel.isOnAndroid()

    @JvmStatic
    fun createButtons(pauseScreen: PauseScreen): List<Button>? {
        if (!isServerConnected()) return null

        val minecraft = Minecraft.getInstance()
        val skinButton = Button.builder(Component.translatable("gui.yes_steve_model.skin")) {
            if (GeneralConfig.DISCLAIMER_SHOW.get()) {
                minecraft.setScreen(DisclaimerScreen())
            } else {
                minecraft.setScreen(PlayerModelScreen())
            }
        }.bounds((pauseScreen.width / 2) - 69, pauseScreen.height - 35, 138, 30).build()
        skinButton.setTooltip(Tooltip.create(Component.translatable("key.yes_steve_model.player_model.desc")))

        val configButton = Button.builder(Component.literal("🔧")) {
            minecraft.setScreen(ExtraPlayerRenderScreen())
        }.bounds((pauseScreen.width / 2) - 120, pauseScreen.height - 35, 50, 30).build()
        configButton.setTooltip(Tooltip.create(Component.translatable("key.yes_steve_model.open_extra_player_render.desc")))

        val rouletteButton = Button.builder(Component.literal("😄")) {
            val player = minecraft.player ?: return@builder
            val cap = PlayerCapability[player] ?: return@builder
            val modelId = cap.getModelId()
            val modelAssembly = cap.getModelAssembly()
            if (modelAssembly != null && modelAssembly.modelData.modelProperties.extraAnimation.isNotEmpty()) {
                minecraft.setScreen(AnimationRouletteScreen(modelId, modelAssembly, cap))
            }
        }.bounds((pauseScreen.width / 2) + 69, pauseScreen.height - 35, 50, 30).build()
        rouletteButton.setTooltip(Tooltip.create(Component.translatable("key.yes_steve_model.animation_roulette.desc")))

        return listOf(skinButton, configButton, rouletteButton)
    }
}