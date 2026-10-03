package com.elfmcys.yesstevemodel.client.gui

import com.elfmcys.yesstevemodel.client.gui.button.FlatColorButton
import com.elfmcys.yesstevemodel.client.upload.ModelUploadSession
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.util.Util
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ModelUploadScreen(private val parentScreen: Screen?) : Screen(Component.literal("upload")), ModelUploadSession.Listener {
    private var lastFlashTime = 0L
    private var error = ""
    private var displayedProgress = 0f
    private var prevProgressTarget = -1f

    override fun init() {
        clearWidgets()
        ModelUploadSession.addListener(this)
        addRenderableWidget(FlatColorButton(width - 70, 10, 60, 18, Component.literal("Back")) {
            minecraft?.setScreen(parentScreen)
        })
    }

    override fun removed() {
        ModelUploadSession.removeListener(this)
        ModelUploadSession.clearIfTerminal()
    }

    override fun onSessionUpdate(session: ModelUploadSession?) {
    }

    override fun onFilesDrop(paths: List<Path>) {
        if (paths.isEmpty()) return

        error = ""
        lastFlashTime = Util.getMillis()
        val path = paths[0]
        val fileName = path.fileName.toString()
        if (!fileName.lowercase().endsWith(".ysm")) {
            error = "Invalid file type, expected .ysm"
            return
        }

        val existing = ModelUploadSession.getInstance()
        if (existing != null && !existing.isTerminal()) {
            error = "Upload already in progress"
            return
        }

        val data: ByteArray = runCatching {
            Files.readAllBytes(path)
        }.getOrElse { e ->
            error = "Failed to read file: ${e.message}"
            return
        }

        val stem = fileName.substring(0, fileName.length - 4)
        val modelId = stem.lowercase()
        if (modelId.isEmpty()) {
            error = "Cannot derive a valid model id from filename: $stem"
            return
        }

        val err = ModelUploadSession.start(modelId, data)
        if (err != null) {
            error = err
        }
    }

    override fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        g.fill(0, 0, width, height, 0xC0000000.toInt())

        val sinceFlash = Util.getMillis() - lastFlashTime
        val borderColor: Int
        val borderWidth: Int
        if (sinceFlash < 900) {
            val t = 1f - (sinceFlash.toFloat() / 900f)
            val alpha = min(255, max(0, (255 * t).toInt()))
            borderColor = (alpha shl 24) or 0x00FFC107
            borderWidth = 4
        } else {
            borderColor = 0x66808080
            borderWidth = 2
        }
        drawBorder(g, 0, 0, width, height, borderWidth, borderColor)

        val session = ModelUploadSession.getInstance()
        if (session == null) {
            renderEmptyState(g)
        } else {
            renderSessionState(g, session)
        }

        if (error.isNotEmpty()) {
            val err = Component.literal(error).withStyle(ChatFormatting.RED)
            val w = font.width(err)
            g.drawString(font, err, (width - w) / 2, height - 60, 0xFFFFFFFF.toInt())
        }

        super.render(g, mouseX, mouseY, partialTick)
    }

    private fun renderEmptyState(guiGraphics: GuiGraphics) {
        val main = Component.literal("Drag a YSM file into this window").withStyle(ChatFormatting.WHITE)
        val sub = Component.literal("Require standalone ysm model file.").withStyle(ChatFormatting.GRAY)
        val cx = width / 2
        val cy = height / 2

        guiGraphics.pose().pushMatrix()
        guiGraphics.pose().translate(cx.toFloat(), (cy - 14).toFloat())
        guiGraphics.pose().scale(2.0f, 2.0f)
        val mw = font.width(main)
        guiGraphics.drawString(font, main, -mw / 2, 0, 0xFFFFFFFF.toInt())
        guiGraphics.pose().popMatrix()

        val sw = font.width(sub)
        guiGraphics.drawString(font, sub, cx - sw / 2, cy + 22, 0xFFAAAAAA.toInt())

        if (ModelUploadSession.hasServerLimits()) {
            val limit = Component.literal("Size limit: " + ModelUploadSession.formatBytes(ModelUploadSession.lastMaxTotalBytes)).withStyle(ChatFormatting.DARK_GRAY)
            val lw = font.width(limit)
            guiGraphics.drawString(font, limit, cx - lw / 2, cy + 36, 0xFFFFFFFF.toInt())
        }
    }

    private fun renderSessionState(guiGraphics: GuiGraphics, session: ModelUploadSession) {
        val cx = width / 2
        val cy = height / 2
        val color = when (session.state) {
            ModelUploadSession.State.COMPLETED -> ChatFormatting.GREEN
            ModelUploadSession.State.FAILED -> ChatFormatting.RED
            else -> ChatFormatting.YELLOW
        }
        val title = Component.literal(session.message).withStyle(color)
        val tw = font.width(title)
        guiGraphics.drawString(font, title, cx - tw / 2, cy - 32, 0xFFFFFFFF.toInt())

        val sub = Component.literal(session.modelId).withStyle(ChatFormatting.GRAY)
        val sw = font.width(sub)
        guiGraphics.drawString(font, sub, cx - sw / 2, cy - 16, 0xFFFFFFFF.toInt())

        val barW = 320
        val barH = 14
        val barX = cx - barW / 2
        val barY = cy + 4
        val target = session.getProgress()
        if (target < prevProgressTarget) {
            displayedProgress = target
        }
        prevProgressTarget = target
        displayedProgress += (target - displayedProgress) * 0.18f
        if (abs(target - displayedProgress) < 0.001f) {
            displayedProgress = target
        }
        val fillW = (barW * displayedProgress).toInt()
        val fillColor = when (session.state) {
            ModelUploadSession.State.FAILED -> 0xFFD23232.toInt()
            ModelUploadSession.State.COMPLETED -> 0xFF4CAF50.toInt()
            else -> 0xFFFFC107.toInt()
        }
        guiGraphics.fill(barX, barY, barX + barW, barY + barH, 0xFF2A2A2A.toInt())
        if (fillW > 0) {
            guiGraphics.fill(barX, barY, barX + fillW, barY + barH, fillColor)
        }
        if (session.state == ModelUploadSession.State.UPLOADING && fillW > 4) {
            val now = Util.getMillis()
            val period = 1400
            val travel = fillW + 40
            val shimmerX = (((now % period).toFloat() / period.toFloat()) * travel).toInt() - 20
            val shimmerW = 24
            val left = barX + max(0, shimmerX)
            val right = barX + min(fillW, shimmerX + shimmerW)
            if (right > left) {
                guiGraphics.fill(left, barY + 1, right, barY + barH - 1, 0x55FFFFFF)
            }
        }
        guiGraphics.fill(barX, barY, barX + barW, barY + 1, -1)
        guiGraphics.fill(barX, barY + barH - 1, barX + barW, barY + barH, -1)
        guiGraphics.fill(barX, barY, barX + 1, barY + barH, -1)
        guiGraphics.fill(barX + barW - 1, barY, barX + barW, barY + barH, -1)

        val stat = "${ModelUploadSession.formatBytes(session.getSentBytes())} / ${ModelUploadSession.formatBytes(session.getTotalBytes())}"
        val statW = font.width(stat)
        guiGraphics.drawString(font, stat, cx - statW / 2, barY + barH + 6, 0xFFAAAAAA.toInt())
    }

    override fun isPauseScreen(): Boolean = false

    override fun onClose() {
        minecraft?.setScreen(parentScreen)
    }

    companion object {
        private fun drawBorder(g: GuiGraphics, x1: Int, y1: Int, x2: Int, y2: Int, w: Int, color: Int) {
            g.fill(x1, y1, x2, y1 + w, color)
            g.fill(x1, y2 - w, x2, y2, color)
            g.fill(x1, y1, x1 + w, y2, color)
            g.fill(x2 - w, y1, x2, y2, color)
        }
    }
}