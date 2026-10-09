package com.elfmcys.yesstevemodel.client.renderer

import com.elfmcys.yesstevemodel.client.ClientModelManager
import com.elfmcys.yesstevemodel.client.ClientOnlyMode
import com.elfmcys.yesstevemodel.config.LoadingStateConfig
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import rip.ysm.api.client.HudOverlay
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class ModelSyncStateOverlay : HudOverlay {

    override fun render(guiGraphics: GuiGraphics, font: Font, partialTick: Float, screenWidth: Int, screenHeight: Int) {
        if (LoadingStateConfig.DISABLE_LOADING_STATE_SCREEN.get()) {
            return
        }

        val textX: Int
        val textY: Int
        val barX: Int
        val barY: Int

        when (LoadingStateConfig.LOADING_STATE_POSITION.get()) {
            LoadingStateConfig.Position.TOP_LEFT -> {
                textX = 10
                textY = 10
                barX = 10
                barY = 22
            }
            LoadingStateConfig.Position.TOP_CENTER -> {
                textX = screenWidth / 2
                textY = 10
                barX = (screenWidth - 150) / 2
                barY = 22
            }
            LoadingStateConfig.Position.TOP_RIGHT -> {
                textX = screenWidth - 10
                textY = 10
                barX = (screenWidth - 10) - 150
                barY = 22
            }
            LoadingStateConfig.Position.BOTTOM_LEFT -> {
                textX = 10
                textY = screenHeight - 30
                barX = 10
                barY = (screenHeight - 8) - 10
            }
            LoadingStateConfig.Position.BOTTOM_CENTER -> {
                textX = screenWidth / 2
                textY = screenHeight - 85
                barX = (screenWidth - 150) / 2
                barY = (screenHeight - 63) - 10
            }
            LoadingStateConfig.Position.BOTTOM_RIGHT -> {
                textX = screenWidth - 10
                textY = screenHeight - 30
                barX = (screenWidth - 10) - 150
                barY = (screenHeight - 8) - 10
            }
            else -> {
                textX = screenWidth / 2
                textY = 10
                barX = (screenWidth - 150) / 2
                barY = 22
            }
        }

        val syncStatus = ClientModelManager.syncStatus
        val isClientOnly = ClientOnlyMode.isActive
        val titleKey = if (isClientOnly) "gui.yes_steve_model.client_hint.title" else "gui.yes_steve_model.sync_hint.title"

        if (syncStatus.currentState == ClientModelManager.SyncState.IDLE) {
            val pendingModelCount = ClientModelManager.pendingModelCount
            if (pendingModelCount > 0) {
                val loadedModelCount = ClientModelManager.modelAssemblyMap.size
                val totalModelCount = loadedModelCount + pendingModelCount
                val loadingTextKey = if (isClientOnly) "gui.yes_steve_model.client_hint.loading_models" else "gui.yes_steve_model.sync_hint.loading_models"
                val loadingText: MutableComponent = Component.translatable(titleKey)
                    .append(
                        Component.translatable(
                            loadingTextKey,
                            pendingModelCount,
                            totalModelCount
                        ).withStyle(ChatFormatting.YELLOW)
                    )
                renderSyncText(font, guiGraphics, loadingText, textX, textY, screenWidth)
                drawAnimatedBar(guiGraphics, barX, barY, loadedModelCount.toFloat() / totalModelCount, 0xFFFFD11A.toInt(), true)
            } else {
                resetAnimation()
            }
            return
        }

        val prefixText: MutableComponent = Component.translatable(titleKey)

        when (syncStatus.currentState) {
            ClientModelManager.SyncState.WAITING -> {
                val waitKey = if (isClientOnly) "gui.yes_steve_model.client_hint.waiting" else "gui.yes_steve_model.sync_hint.waiting"
                prefixText.append(Component.translatable(waitKey).withStyle(ChatFormatting.AQUA))
                resetAnimation()
            }
            ClientModelManager.SyncState.LOADING -> {
                val loadKey = if (isClientOnly) "gui.yes_steve_model.client_hint.loading" else "gui.yes_steve_model.sync_hint.loading"
                prefixText.append(Component.translatable(loadKey).withStyle(ChatFormatting.GOLD))
                resetAnimation()
            }
            ClientModelManager.SyncState.PREPARING -> {
                val prepKey = if (isClientOnly) "gui.yes_steve_model.client_hint.preparing" else "gui.yes_steve_model.sync_hint.preparing"
                prefixText.append(Component.translatable(prepKey).withStyle(ChatFormatting.LIGHT_PURPLE))
                resetAnimation()
            }
            ClientModelManager.SyncState.SYNCING -> {
                if (syncStatus.syncedModels == 0) {
                    val syncKey = if (isClientOnly) "gui.yes_steve_model.client_hint.loading" else "gui.yes_steve_model.sync_hint.syncing"
                    prefixText.append(Component.translatable(syncKey).withStyle(ChatFormatting.RED))
                    resetAnimation()
                } else {
                    prefixText.append(
                        Component.literal("${syncStatus.syncedModels}/${syncStatus.totalModels}")
                            .withStyle(ChatFormatting.GREEN)
                    )
                    drawAnimatedBar(
                        guiGraphics,
                        barX,
                        barY,
                        syncStatus.syncedModels.toFloat() / syncStatus.totalModels,
                        0xFF55FF55.toInt(),
                        true
                    )
                }
            }
            else -> {}
        }
        renderSyncText(font, guiGraphics, prefixText, textX, textY, screenWidth)
    }

    private fun renderSyncText(font: Font, guiGraphics: GuiGraphics, textComponent: MutableComponent, baseX: Int, textY: Int, screenWidth: Int) {
        val textWidth = font.width(textComponent)
        val drawX = when (LoadingStateConfig.LOADING_STATE_POSITION.get()) {
            LoadingStateConfig.Position.TOP_LEFT, LoadingStateConfig.Position.BOTTOM_LEFT -> baseX
            LoadingStateConfig.Position.TOP_CENTER, LoadingStateConfig.Position.BOTTOM_CENTER -> (screenWidth - textWidth) / 2
            LoadingStateConfig.Position.TOP_RIGHT, LoadingStateConfig.Position.BOTTOM_RIGHT -> baseX - textWidth
            else -> (screenWidth - textWidth) / 2
        }
        guiGraphics.drawString(font, textComponent, drawX, textY, -1)
    }

    companion object {
        private const val BAR_WIDTH = 150
        private const val BAR_HEIGHT = 10
        private const val BAR_BG = 0xFF555555.toInt()

        private var animatedProgress = 0.0f
        private var lastFrameNanos = 0L
        private var shimmerPhase = 0.0f

        @JvmStatic
        fun render(graphics: GuiGraphics, partialTick: Float) {
        }

        private fun drawAnimatedBar(g: GuiGraphics, x: Int, y: Int, targetVal: Float, fgColor: Int, shimmer: Boolean) {
            val now = System.nanoTime()
            if (lastFrameNanos == 0L) lastFrameNanos = now
            val dt = min(0.1f, ((now - lastFrameNanos) / 1.0e9).toFloat())
            lastFrameNanos = now
            val lerp = 1.0f - exp(-dt * 12.0f)
            val target = max(0.0f, min(1.0f, targetVal))
            animatedProgress += (target - animatedProgress) * lerp
            if (kotlin.math.abs(target - animatedProgress) < 0.001f) animatedProgress = target
            shimmerPhase = (shimmerPhase + dt * 1.2f) % 1.0f

            g.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, BAR_BG)
            val fillW = (animatedProgress * BAR_WIDTH).roundToInt()
            if (fillW > 0) {
                g.fill(x, y, x + fillW, y + BAR_HEIGHT, fgColor)
                val highlight = (fgColor and 0x00FFFFFF) or 0x60000000
                g.fill(x, y, x + fillW, y + 1, lighten(fgColor))
                if (shimmer && fillW > 8) {
                    val shimmerX = x + (shimmerPhase * fillW).roundToInt()
                    val shimmerW = min(12, fillW - (shimmerX - x))
                    if (shimmerW > 0) {
                        g.fill(shimmerX, y, shimmerX + shimmerW, y + BAR_HEIGHT, highlight)
                    }
                }
            }
        }

        private fun lighten(argb: Int): Int {
            val a = (argb ushr 24) and 0xFF
            val r = min(255, ((argb shr 16) and 0xFF) + 60)
            val gn = min(255, ((argb shr 8) and 0xFF) + 60)
            val b = min(255, (argb and 0xFF) + 60)
            return (a shl 24) or (r shl 16) or (gn shl 8) or b
        }

        private fun resetAnimation() {
            animatedProgress = 0.0f
            lastFrameNanos = 0L
            shimmerPhase = 0.0f
        }
    }
}
