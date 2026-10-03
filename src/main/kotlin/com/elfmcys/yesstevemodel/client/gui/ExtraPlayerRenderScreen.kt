package com.elfmcys.yesstevemodel.client.gui

import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer
import com.elfmcys.yesstevemodel.config.ExtraPlayerRenderConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Checkbox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import kotlin.math.min

class ExtraPlayerRenderScreen : Screen(Component.literal("YSM Extra Player Render Config GUI")) {
    private var mouseStartX: Int = ExtraPlayerRenderConfig.PLAYER_POS_X.get()
    private var mouseStartY: Int = ExtraPlayerRenderConfig.PLAYER_POS_Y.get()
    private var rotationX: Float = ExtraPlayerRenderConfig.PLAYER_SCALE.get().toFloat()
    private var rotationY: Float = ExtraPlayerRenderConfig.PLAYER_YAW_OFFSET.get().toFloat()
    private var isDragging: Boolean = false
    private var isRightDragging: Boolean = false
    private var offsetX: Int = if (PauseScreenButtonBuilder.isServerConnected()) 16 else 5
    private var offsetY: Int = if (PauseScreenButtonBuilder.isServerConnected()) 0 else 1

    override fun init() {
        clearWidgets()
        var i = -30
        if (PauseScreenButtonBuilder.isServerConnected()) {
            addRenderableWidget(
                Button.builder(Component.translatable("controls.reset")) {
                    resetTransform()
                }.bounds((width / 2) - 50, height - 35, 100, 30).build()
            )
            i = -60
        }
        val label: MutableComponent = Component.translatable("gui.yes_steve_model.hide_or_show")
        val labelWidth = font.width(label) + 24
        addRenderableWidget(
            Checkbox.builder(label, font)
                .pos((width - labelWidth) / 2, height + i)
                .selected(ExtraPlayerRenderConfig.DISABLE_PLAYER_RENDER.get())
                .onValueChange { _, value ->
                    ExtraPlayerRenderConfig.DISABLE_PLAYER_RENDER.set(value)
                }
                .build()
        )
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val boxLeft = mouseStartX
        val boxTop = mouseStartY
        val boxRight = (boxLeft + rotationX).toInt()
        val boxBottom = (boxTop + (rotationX * 2.0f)).toInt()

        guiGraphics.vLine((width / 2) - 1, -2, height + 2, -1610612737)
        guiGraphics.hLine(-2, width + 2, (height / 2) - 1, -1610612737)
        guiGraphics.vLine(10, -2, height + 2, -1610612737)
        guiGraphics.vLine(width - 10, -2, height + 2, -1610612737)
        guiGraphics.hLine(-2, width + 2, 10, -1610612737)
        guiGraphics.hLine(-2, width + 2, height - 10, -1610612737)

        guiGraphics.vLine(boxLeft, boxTop, boxBottom, -65536)
        guiGraphics.vLine(boxRight, boxTop, boxBottom, -65536)
        guiGraphics.hLine(boxLeft, boxRight, boxTop, -65536)
        guiGraphics.hLine(boxLeft, boxRight, boxBottom, -65536)

        guiGraphics.fillGradient(boxLeft, boxTop, boxRight, boxBottom, 1342177279, 1342177279)
        guiGraphics.fillGradient(boxLeft - offsetX, boxTop - offsetX, boxLeft + offsetX, boxTop + offsetX, -16711777, -16711777)
        guiGraphics.fillGradient(boxRight - offsetX, boxBottom - offsetX, boxRight + offsetX, boxBottom + offsetX, -16777057, -16777057)

        var tipY = 15
        for (formattedCharSequence in font.split(Component.translatable("gui.yes_steve_model.extra_player_render.tips"), 500)) {
            guiGraphics.drawString(font, formattedCharSequence, (width - 15) - font.width(formattedCharSequence), tipY, -1)
            tipY += 10
        }

        val player = Minecraft.getInstance().player
        if (player != null && !ExtraPlayerRenderConfig.DISABLE_PLAYER_RENDER.get()) {
            ModelPreviewRenderer.submitPlayerOverlay(
                guiGraphics,
                player,
                mouseStartX.toDouble(),
                mouseStartY.toDouble(),
                rotationX,
                rotationY,
                minecraft?.deltaTracker?.getGameTimeDeltaPartialTick(false) ?: partialTick
            )
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    override fun renderBlurredBackground(guiGraphics: GuiGraphics) {
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val mouseX = event.x()
        val mouseY = event.y()
        val button = event.button()
        val inLeftHandleX = (mouseStartX - offsetX).toDouble() < mouseX && mouseX < (mouseStartX + offsetX).toDouble()
        val inLeftHandleY = (mouseStartY - offsetX).toDouble() < mouseY && mouseY < (mouseStartY + offsetX).toDouble()
        if (button == 0 && inLeftHandleX && inLeftHandleY) {
            isDragging = true
        }
        val rightHandleX = (mouseStartX + rotationX).toInt()
        val rightHandleY = (mouseStartY + (rotationX * 2.0f)).toInt()
        val inRightHandleX = (rightHandleX - offsetX).toDouble() < mouseX && mouseX < (rightHandleX + offsetX).toDouble()
        val inRightHandleY = (rightHandleY - offsetX).toDouble() < mouseY && mouseY < (rightHandleY + offsetX).toDouble()
        if (button == 0 && inRightHandleX && inRightHandleY) {
            isRightDragging = true
        }
        return super.mouseClicked(event, doubleClick)
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        isDragging = false
        isRightDragging = false
        return super.mouseReleased(event)
    }

    override fun mouseDragged(event: MouseButtonEvent, dragX: Double, dragY: Double): Boolean {
        val mouseX = event.x()
        val mouseY = event.y()
        if (isRightDragging) {
            rotationX = min(mouseX - mouseStartX, (mouseY - mouseStartY) / 2.0).toFloat()
            return true
        }
        if (isDragging) {
            mouseStartX = mouseX.toInt()
            mouseStartY = mouseY.toInt()
            return true
        }
        if (event.button() == offsetY) {
            rotationY += (dragX * 2.0).toFloat()
            return true
        }
        return false
    }

    override fun charTyped(event: CharacterEvent): Boolean {
        val codePoint = event.codepoint()
        if (codePoint.toChar().lowercaseChar() == RESET_KEY && Minecraft.getInstance().hasAltDown()) {
            resetTransform()
        }
        return super.charTyped(event)
    }

    private fun resetTransform() {
        mouseStartX = 10
        mouseStartY = 10
        rotationX = 40.0f
        rotationY = 5.0f
    }

    override fun onClose() {
        ExtraPlayerRenderConfig.PLAYER_POS_X.set(mouseStartX)
        ExtraPlayerRenderConfig.PLAYER_POS_Y.set(mouseStartY)
        ExtraPlayerRenderConfig.PLAYER_SCALE.set(rotationX.toDouble())
        ExtraPlayerRenderConfig.PLAYER_YAW_OFFSET.set(rotationY.toDouble())
        super.onClose()
    }

    companion object {
        const val RESET_KEY: Char = 'r'
    }
}