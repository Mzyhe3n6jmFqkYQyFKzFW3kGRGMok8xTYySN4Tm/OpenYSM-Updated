package com.elfmcys.yesstevemodel.client.gui.button

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.NameSpaces
import com.elfmcys.yesstevemodel.client.gui.ISpecialWidget
import com.elfmcys.yesstevemodel.config.ServerConfig
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache
import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.network.message.C2SRequestExecuteMolangPacket
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.util.Mth
import java.text.DecimalFormat

class AnimationSlider(
    x: Int,
    y: Int,
    component: Component,
    currentValue: Double,
    private val model: AnimatableEntity<*>,
    private val controllerName: String,
    stepSize: Double,
    minValue: Double,
    maxValue: Double
) : RangedSliderWidget(
    x,
    y,
    115,
    15,
    component,
    Component.empty(),
    minValue,
    maxValue,
    currentValue,
    stepSize,
    0,
    true
), ISpecialWidget {

    override fun applyValue() {
        runCatching {
            val str = "$controllerName=${getValue()}"
            model.executeExpression(GeckoLibCache.parseSimpleExpression(str), true, false, null)
            val entity = model.entity
            if (!GeckoLibCache.isRoamingVariableAssignment(str) && NetworkHandler.isClientConnected() && !ServerConfig.LOW_BANDWIDTH_USAGE.get())
                NetworkHandler.sendToServer(C2SRequestExecuteMolangPacket(str, entity.id))
        }.onFailure { e ->
            Constants.LOGGER.error(e.message, e)
        }
    }

    override val valueString: String
        get() {
            return DECIMAL_FORMAT.format(getValue())
        }

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val minecraft = Minecraft.getInstance()
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ROULETTE_TEXTURE, x, y, 0.0f, 24.0f, width - 4, height, 256, 256)
        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED,
            ROULETTE_TEXTURE,
            x + width - 4,
            y,
            196.0f,
            24.0f,
            4,
            height,
            256,
            256
        )
        val handleOffset = (value * (width - 8)).toInt()
        val vOffset = if (isHovered) 84.0f else 64.0f
        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED,
            ROULETTE_TEXTURE,
            x + handleOffset,
            y,
            0.0f,
            vOffset,
            4,
            height,
            256,
            256
        )
        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED,
            ROULETTE_TEXTURE,
            x + handleOffset + 4,
            y,
            196.0f,
            vOffset,
            4,
            height,
            256,
            256
        )
        val color = 0x00FFFFFF or (Mth.ceil(alpha * 255.0f) shl 24)
        guiGraphics.drawCenteredString(minecraft.font, message, x + (width / 2), y + ((height - 8) / 2), color)
    }

    companion object {
        val ROULETTE_TEXTURE: Identifier = NameSpaces.MOD.path("texture/roulette.png")
        private val DECIMAL_FORMAT: DecimalFormat = DecimalFormat("#.##")
    }
}