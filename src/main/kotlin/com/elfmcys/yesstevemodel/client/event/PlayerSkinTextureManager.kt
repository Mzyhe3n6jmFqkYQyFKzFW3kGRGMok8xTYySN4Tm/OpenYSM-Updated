@file:Suppress("MemberVisibilityCanBePrivate")

package com.elfmcys.yesstevemodel.client.event

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.client.ClientModelManager
import com.elfmcys.yesstevemodel.client.entity.PlayerPreviewEntity
import com.elfmcys.yesstevemodel.event.api.SpecialPlayerRenderEvent
import com.elfmcys.yesstevemodel.model.ServerModelManager
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

    val WIDE_DEFAULT_SKINS: List<Identifier> = listOf(
        STEVE_SKIN,
        Identifier.parse("textures/entity/player/wide/alex.png"),
        Identifier.parse("textures/entity/player/wide/ari.png"),
        Identifier.parse("textures/entity/player/wide/efe.png"),
        Identifier.parse("textures/entity/player/wide/kai.png"),
        Identifier.parse("textures/entity/player/wide/makena.png"),
        Identifier.parse("textures/entity/player/wide/noor.png"),
        Identifier.parse("textures/entity/player/wide/sunny.png"),
        Identifier.parse("textures/entity/player/wide/zuri.png"),
    )

    val SLIM_DEFAULT_SKINS: List<Identifier> = listOf(
        ALEX_SKIN,
        Identifier.parse("textures/entity/player/slim/steve.png"),
        Identifier.parse("textures/entity/player/slim/ari.png"),
        Identifier.parse("textures/entity/player/slim/efe.png"),
        Identifier.parse("textures/entity/player/slim/kai.png"),
        Identifier.parse("textures/entity/player/slim/makena.png"),
        Identifier.parse("textures/entity/player/slim/noor.png"),
        Identifier.parse("textures/entity/player/slim/sunny.png"),
        Identifier.parse("textures/entity/player/slim/zuri.png"),
    )

    init {
        SpecialPlayerRenderEvent.EVENT.register(::onRenderTexture)
    }

    private fun onRenderTexture(event: SpecialPlayerRenderEvent): EventResult {
        if (!YesSteveModel.isAvailable) return EventResult.pass()
        val modelId = event.modelId ?: return EventResult.pass()
        if (isCustomSkinModel(modelId) || getUseMcDefaultTexture(modelId) > 0) {
            val location = getPlayerSkinLocation(event.player, modelId)
            if (location != null) {
                event.textureLocation = location
            }
        }
        return EventResult.pass()
    }

    fun isCustomSkinModel(modelId: String?): Boolean =
        modelId != null && ClientModelManager.isCustomSkinModel(modelId)

    fun getUseMcDefaultTexture(modelId: String?): Int {
        if (modelId == null) return 0
        val clientVal = ClientModelManager.getUseMcDefaultTexture(modelId)
        if (clientVal != 0) return clientVal
        return ServerModelManager.getUseMcDefaultTexture(modelId)
    }

    fun getDefaultSkinTexture(useMcDefaultTexture: Int, player: Player? = null): Identifier? {
        val targetPlayer = if (PlayerPreviewEntity.isPreviewPlayer(player) || player == null) {
            runCatching { Minecraft.getInstance().player }.getOrNull() ?: player
        } else {
            player
        }

        return when (useMcDefaultTexture) {
            1 -> STEVE_SKIN
            2 -> ALEX_SKIN
            3 -> {
                if (targetPlayer != null) {
                    val index = Math.floorMod(targetPlayer.uuid.hashCode(), WIDE_DEFAULT_SKINS.size)
                    WIDE_DEFAULT_SKINS[index]
                } else {
                    STEVE_SKIN
                }
            }

            4 -> {
                if (targetPlayer != null) {
                    val index = Math.floorMod(targetPlayer.uuid.hashCode(), SLIM_DEFAULT_SKINS.size)
                    SLIM_DEFAULT_SKINS[index]
                } else {
                    ALEX_SKIN
                }
            }

            else -> null
        }
    }

    fun getSkinTexture(str: String): Identifier {
        val defaultType = getUseMcDefaultTexture(str)
        val defaultTex = getDefaultSkinTexture(defaultType)
        if (defaultTex != null) return defaultTex
        return if (str.contains("alex", ignoreCase = true) || str == "misc/1_alex") ALEX_SKIN else STEVE_SKIN
    }

    fun getPlayerSkinLocation(player: Player?, modelId: String? = null): Identifier? {
        val targetPlayer = if (PlayerPreviewEntity.isPreviewPlayer(player) || player == null) {
            runCatching { Minecraft.getInstance().player }.getOrNull() ?: player
        } else {
            player
        }

        val isCustomSkin = isCustomSkinModel(modelId)

        if (isCustomSkin && targetPlayer is AbstractClientPlayer) {
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

        if (modelId != null) {
            val defaultType = getUseMcDefaultTexture(modelId)
            if (defaultType > 0) {
                return getDefaultSkinTexture(defaultType, targetPlayer)
            }
            return getSkinTexture(modelId)
        }
        return null
    }
}
