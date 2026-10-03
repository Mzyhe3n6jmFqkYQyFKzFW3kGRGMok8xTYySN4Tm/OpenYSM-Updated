package com.elfmcys.yesstevemodel.client.renderer

import com.elfmcys.yesstevemodel.client.gui.ExtraPlayerRenderScreen
import com.elfmcys.yesstevemodel.config.ExtraPlayerRenderConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.player.LocalPlayer
import rip.ysm.api.client.HudOverlay

open class ExtraPlayerOverlay : HudOverlay {
    override fun render(guiGraphics: GuiGraphics, font: Font, partialTick: Float, screenWidth: Int, screenHeight: Int) {
        val minecraft = Minecraft.getInstance()
        val localPlayer: LocalPlayer = minecraft.player ?: return
        if (ExtraPlayerRenderConfig.DISABLE_PLAYER_RENDER.get() || minecraft.screen is ExtraPlayerRenderScreen) {
            return
        }
        ModelPreviewRenderer.submitPlayerOverlay(
            guiGraphics,
            localPlayer,
            ExtraPlayerRenderConfig.PLAYER_POS_X.get().toDouble(),
            ExtraPlayerRenderConfig.PLAYER_POS_Y.get().toDouble(),
            ExtraPlayerRenderConfig.PLAYER_SCALE.get().toFloat(),
            ExtraPlayerRenderConfig.PLAYER_YAW_OFFSET.get().toFloat(),
            minecraft.deltaTracker.getGameTimeDeltaPartialTick(false)
        )
    }
}