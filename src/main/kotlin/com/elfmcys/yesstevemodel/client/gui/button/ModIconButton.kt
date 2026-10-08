package com.elfmcys.yesstevemodel.client.gui.button

import com.elfmcys.yesstevemodel.NameSpaces
import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.capability.StarModelsCapability
import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.network.message.C2SSetStarModelPacket
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.InputWithModifiers
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier

class ModIconButton(x: Int, y: Int) : FlatColorButton(x, y, 20, 20, Component.empty(), {}) {

    override fun renderContents(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.renderContents(guiGraphics, mouseX, mouseY, partialTick)
        val iconOffsetX = (width - 16) / 2
        val iconOffsetY = (height - 16) / 2
        val localPlayer = Minecraft.getInstance().player ?: return
        val cap = PlayerCapability[localPlayer] ?: return
        val starCap = StarModelsCapability[localPlayer] ?: return
        val u = if (starCap.containsModel(cap.modelId)) 16.0f else 0.0f
        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED,
            ICON_TEXTURE,
            x + iconOffsetX,
            y + iconOffsetY,
            u,
            0.0f,
            16,
            16,
            256,
            256
        )
    }

    override fun onPress(input: InputWithModifiers) {
        val localPlayer = Minecraft.getInstance().player ?: return
        val cap = PlayerCapability[localPlayer] ?: return
        val starCap = StarModelsCapability[localPlayer] ?: return
        val modelId = cap.modelId
        if (starCap.containsModel(modelId)) {
            starCap.removeModel(modelId)
            NetworkHandler.sendToServer(C2SSetStarModelPacket.remove(modelId))
        } else {
            starCap.addModel(modelId)
            NetworkHandler.sendToServer(C2SSetStarModelPacket.add(modelId))
        }
    }

    companion object {
        val ICON_TEXTURE: Identifier = NameSpaces.MOD.path("texture/icon.png")
    }
}