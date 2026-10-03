package com.elfmcys.yesstevemodel.client.gui.button

import net.minecraft.client.InputType
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractSliderButton
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.util.Mth
import org.lwjgl.glfw.GLFW
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt

abstract class RangedSliderWidget(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    val prefix: Component,
    val suffix: Component,
    val minValue: Double,
    val maxValue: Double,
    currentValue: Double,
    stepSize: Double,
    precision: Int,
    val drawString: Boolean
) : AbstractSliderButton(x, y, width, height, Component.empty(), 0.0) {
    private val stepSize: Double = abs(stepSize)
    private val format: DecimalFormat

    init {
        value = snapToNearest((currentValue - minValue) / (maxValue - minValue))
        if (stepSize == 0.0) {
            val p = min(precision, 4)
            val builder = StringBuilder("0")
            if (p > 0) builder.append('.')
            for (i in 0 until p) builder.append('0')
            format = DecimalFormat(builder.toString())
        } else {
            format =
                if (Mth.equal(stepSize, floor(stepSize))) DecimalFormat("0") else DecimalFormat(
                    stepSize.toString().replace(Regex("\\d"), "0")
                )
        }
        updateMessage()
    }

    constructor(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        prefix: Component,
        suffix: Component,
        minValue: Double,
        maxValue: Double,
        currentValue: Double,
        drawString: Boolean
    ) : this(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, 1.0, 0, drawString)

    fun getValue(): Double = (value * (maxValue - minValue)) + minValue

    override fun setValue(newValue: Double) {
        val oldValue = value
        value = snapToNearest((newValue - minValue) / (maxValue - minValue))
        if (!Mth.equal(oldValue, value)) applyValue()
        updateMessage()
    }

    open fun getValueString(): String = format.format(getValue())

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) = setValueFromMouse(event.x())

    override fun onDrag(event: MouseButtonEvent, dragX: Double, dragY: Double) {
        super.onDrag(event, dragX, dragY)
        setValueFromMouse(event.x())
    }

    override fun setFocused(focused: Boolean) {
        super.setFocused(focused)
        if (!focused) {
            canChangeValue = false
        } else {
            val inputType = Minecraft.getInstance().lastInputType
            if (inputType == InputType.MOUSE || inputType == InputType.KEYBOARD_TAB) {
                canChangeValue = true
            }
        }
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        val keyCode = event.key()
        var leftDir = keyCode == GLFW.GLFW_KEY_LEFT
        if (leftDir || keyCode == GLFW.GLFW_KEY_RIGHT) {
            if (minValue > maxValue) leftDir = !leftDir
            val dir = if (leftDir) -1f else 1f
            if (stepSize <= 0.0) setSliderValue(value + (dir / (width - 8))) else setValue(getValue() + (dir * stepSize))
        }
        return false
    }

    private fun setValueFromMouse(mouseX: Double) {
        setSliderValue((mouseX - (x + 4)) / (width - 8))
    }

    private fun setSliderValue(newValue: Double) {
        val oldValue = value
        value = snapToNearest(newValue)
        if (!Mth.equal(oldValue, value)) applyValue()
        updateMessage()
    }

    private fun snapToNearest(raw: Double): Double {
        if (stepSize <= 0.0) return Mth.clamp(raw, 0.0, 1.0)
        var clamped = Mth.lerp(Mth.clamp(raw, 0.0, 1.0), minValue, maxValue)
        clamped = stepSize * (clamped / stepSize).roundToInt()
        clamped =
            if (minValue > maxValue) Mth.clamp(clamped, maxValue, minValue) else Mth.clamp(clamped, minValue, maxValue)
        return Mth.map(clamped, minValue, maxValue, 0.0, 1.0)
    }

    override fun updateMessage() {
        message = if (drawString) Component.literal("").append(prefix).append(getValueString())
            .append(suffix) else Component.empty()
    }

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val mc = Minecraft.getInstance()
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SLIDER_TEXTURE, x, y, 0.0f, 0.0f, width, height, 200, height)
        val handleTexture = if (isHovered) SLIDER_HANDLE_HIGHLIGHTED_TEXTURE else SLIDER_HANDLE_TEXTURE
        val handleX = x + (value * (width - 8)).toInt()
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, handleTexture, handleX, y, 0.0f, 0.0f, 8, height, 8, height)
        val color = 0x00FFFFFF or (Mth.ceil(alpha * 255.0f) shl 24)
        guiGraphics.drawCenteredString(mc.font, message, x + (width / 2), y + ((height - 8) / 2), color)
    }

    companion object {
        val SLIDER_TEXTURE: Identifier = Identifier.parse("textures/gui/sprites/widget/slider.png")
        val SLIDER_HIGHLIGHTED_TEXTURE: Identifier =
            Identifier.parse("textures/gui/sprites/widget/slider_highlighted.png")
        val SLIDER_HANDLE_TEXTURE: Identifier = Identifier.parse("textures/gui/sprites/widget/slider_handle.png")
        val SLIDER_HANDLE_HIGHLIGHTED_TEXTURE: Identifier =
            Identifier.parse("textures/gui/sprites/widget/slider_handle_highlighted.png")
    }
}