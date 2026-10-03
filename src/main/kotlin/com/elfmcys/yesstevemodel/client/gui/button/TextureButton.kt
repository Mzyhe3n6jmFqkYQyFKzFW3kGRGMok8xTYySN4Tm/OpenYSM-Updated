package com.elfmcys.yesstevemodel.client.gui.button

import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.client.entity.PlayerPreviewEntity
import com.elfmcys.yesstevemodel.client.gui.ModelMetadataPresenter
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer
import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.network.message.C2SRequestSwitchModelPacket
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.input.InputWithModifiers
import net.minecraft.network.chat.Component

open class TextureButton(
    x: Int,
    y: Int,
    val previewEntity: PlayerPreviewEntity,
    val modelAssembly: ModelAssembly
) : Button(x, y, 54, 102, Component.empty(), {}, DEFAULT_NARRATION) {

    override fun onPress(input: InputWithModifiers) {
        val localPlayer = Minecraft.getInstance().player ?: return
        val cap = PlayerCapability[localPlayer] ?: return
        val textureName = previewEntity.getCurrentTextureName() ?: ""
        cap.setCurrentTexture(textureName)
        NetworkHandler.sendToServer(C2SRequestSwitchModelPacket(previewEntity.getModelId(), textureName))
    }

    override fun renderContents(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val minecraft = Minecraft.getInstance()
        val font = minecraft.font
        guiGraphics.fillGradient(x, y, x + width, y + height, -12369342, -12369342)
        renderPlayerPreview(guiGraphics, minecraft.deltaTracker.getGameTimeDeltaPartialTick(false))
        val str = previewEntity.getCurrentTextureName() ?: ""
        val component = Component.literal(
            ModelMetadataPresenter.getLocalizedModelString(
                modelAssembly,
                "files.player.texture.$str",
                str
            )
        )
        val listSplit = font.split(component, 50)
        if (listSplit.size > 1) {
            guiGraphics.drawCenteredString(font, listSplit[0], x + (width / 2), (y + height) - 19, 0xFFF3F0E0.toInt())
            guiGraphics.drawCenteredString(font, listSplit[1], x + (width / 2), (y + height) - 10, 0xFFF3F0E0.toInt())
        } else {
            guiGraphics.drawCenteredString(font, component, x + (width / 2), (y + height) - 15, 0xFFF3F0E0.toInt())
        }
        if (isHoveredOrFocused) {
            guiGraphics.fillGradient(x, y + 1, x + 1, (y + height) - 1, -790560, -790560)
            guiGraphics.fillGradient(x, y, x + width, y + 1, -790560, -790560)
            guiGraphics.fillGradient((x + width) - 1, y + 1, x + width, (y + height) - 1, -790560, -790560)
            guiGraphics.fillGradient(x, (y + height) - 1, x + width, y + height, -790560, -790560)
        }
    }

    open fun renderPlayerPreview(guiGraphics: GuiGraphics, partialTick: Float) {
        ModelPreviewRenderer.submitLivingEntityPreview(
            guiGraphics,
            x,
            y,
            x + width,
            (y + height) - 20,
            35,
            partialTick,
            previewEntity,
            false,
            true
        )
    }
}