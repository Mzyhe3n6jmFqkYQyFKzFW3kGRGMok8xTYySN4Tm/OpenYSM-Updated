@file:Suppress("MemberVisibilityCanBePrivate")

package com.elfmcys.yesstevemodel.client.event

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.client.ClientModelManager
import com.elfmcys.yesstevemodel.client.entity.PlayerPreviewEntity
import com.elfmcys.yesstevemodel.event.api.SpecialPlayerRenderEvent
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.Minecraft
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Player
import rip.ysm.api.event.EventResult

@Environment(EnvType.CLIENT)
object PlayerSkinTextureManager {
    val STEVE_SKIN: Identifier = Identifier.parse("textures/entity/player/wide/steve.png")
    val ALEX_SKIN: Identifier = Identifier.parse("textures/entity/player/slim/alex.png")

    init {
        SpecialPlayerRenderEvent.EVENT.register(::onRenderTexture)
    }

    private fun onRenderTexture(event: SpecialPlayerRenderEvent): EventResult {
        if (!YesSteveModel.isAvailable) return EventResult.pass()
        val modelId = event.modelId ?: return EventResult.pass()
        if (isCustomSkinModel(modelId)) {
            val location = getPlayerSkinLocation(event.player, modelId)
            if (location != null) {
                event.textureLocation = location
            }
        }
        return EventResult.pass()
    }

    fun isCustomSkinModel(modelId: String?): Boolean =
        modelId != null && ClientModelManager.isCustomSkinModel(modelId)

    fun getSkinTexture(str: String): Identifier =
        if (str.contains("alex", ignoreCase = true) || str == "misc/1_alex") ALEX_SKIN else STEVE_SKIN

    fun getPlayerSkinLocation(player: Player?, modelId: String? = null): Identifier? {
        val targetPlayer = if (PlayerPreviewEntity.isPreviewPlayer(player) || player == null) {
            runCatching { Minecraft.getInstance().player }.getOrNull() ?: player
        } else {
            player
        }

        if (targetPlayer is AbstractClientPlayer) {
            val loc = runCatching {
                targetPlayer.skin.body().texturePath()
            }.getOrNull()
            if (loc != null) return loc

            val lookupLoc = runCatching {
                val minecraft = Minecraft.getInstance()
                val skinLookup = minecraft.skinManager.createLookup(targetPlayer.gameProfile, false)
                skinLookup.get().body().texturePath()
            }.getOrNull()
            if (lookupLoc != null) return lookupLoc
        }

        if (modelId != null) return getSkinTexture(modelId)
        return null
    }
}
