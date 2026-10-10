package com.elfmcys.yesstevemodel.client.gui.button

import com.elfmcys.yesstevemodel.NameSpaces
import com.elfmcys.yesstevemodel.client.gui.ModelMetadataPresenter
import com.elfmcys.yesstevemodel.resource.models.ModelPackData
import com.elfmcys.yesstevemodel.util.FileTypeUtil
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.util.FormattedCharSequence

class PackIconButton(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val packData: ModelPackData,
    onPress: OnPress
) : Button(
    x,
    y,
    width,
    height,
    Component.literal(ModelMetadataPresenter.getLocalizedString(packData, "name", packData.name)),
    onPress,
    DEFAULT_NARRATION
) {

    override fun renderContents(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val minecraft = Minecraft.getInstance()
        val font = minecraft.font
        guiGraphics.fillGradient(x, y, x + width, y + height, -6598176, -6598176)
        val location = FileTypeUtil.getPackIconLocation(packData.path)
        val texture = minecraft.textureManager.getTexture(location)
        val missing = location == MissingTextureAtlasSprite.getLocation()
        val iconToDraw = if (missing) DEFAULT_PACK_ICON else location
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, iconToDraw, x, y, 0.0f, 0.0f, width, height, width, height)

        val msg = getMessage()
        val listSplit = font.split(msg, 45)
        if (listSplit.size > 1) {
            drawCenteredString(guiGraphics, font, listSplit[0], x + (width / 2), (y + height) - 19, 0xFF555555.toInt())
            drawCenteredString(guiGraphics, font, listSplit[1], x + (width / 2), (y + height) - 10, 0xFF555555.toInt())
        } else {
            drawCenteredString(guiGraphics, font, msg, x + (width / 2), (y + height) - 15, 0xFF555555.toInt())
        }
        if (isHoveredOrFocused) {
            guiGraphics.fillGradient(x, y + 1, x + 1, (y + height) - 1, -1982745, -1982745)
            guiGraphics.fillGradient(x, y, x + width, y + 1, -1982745, -1982745)
            guiGraphics.fillGradient((x + width) - 1, y + 1, x + width, (y + height) - 1, -1982745, -1982745)
            guiGraphics.fillGradient(x, (y + height) - 1, x + width, y + height, -1982745, -1982745)
        }
    }

    fun renderDescription(guiGraphics: GuiGraphics, screen: Screen, mouseX: Int, mouseY: Int) {
        val desc = packData.description
        val str = ModelMetadataPresenter.getLocalizedString(packData, "description", desc ?: "")
        if (str.isBlank()) return
        if (isHovered) {
            guiGraphics.setComponentTooltipForNextFrame(
                Minecraft.getInstance().font,
                listOf(Component.literal(str)),
                mouseX,
                mouseY
            )
        }
    }

    companion object {
        val DEFAULT_PACK_ICON: Identifier = NameSpaces.MOD.path("texture/default_pack_icon.png")

        fun drawCenteredString(
            guiGraphics: GuiGraphics,
            font: Font,
            component: Component,
            centerX: Int,
            y: Int,
            color: Int
        ) {
            guiGraphics.drawString(font, component, centerX - (font.width(component) / 2), y, color, false)
        }

        fun drawCenteredString(
            guiGraphics: GuiGraphics,
            font: Font,
            formattedCharSequence: FormattedCharSequence,
            centerX: Int,
            y: Int,
            color: Int
        ) {
            guiGraphics.drawString(
                font,
                formattedCharSequence,
                centerX - (font.width(formattedCharSequence) / 2),
                y,
                color,
                false
            )
        }
    }
}