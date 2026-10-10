@file:Suppress("MemberVisibilityCanBePrivate")

package com.elfmcys.yesstevemodel.client.event

import com.elfmcys.yesstevemodel.NameSpaces
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
import net.minecraft.world.entity.player.PlayerSkin
import rip.ysm.api.event.EventResult
import kotlin.jvm.optionals.getOrNull

@Environment(EnvType.CLIENT)
object PlayerSkinTextureManager {
    val STEVE_SKIN: Identifier = NameSpaces.MINECRAFT.path("textures/entity/player/wide/steve.png")
    val ALEX_SKIN: Identifier = NameSpaces.MINECRAFT.path("textures/entity/player/slim/alex.png")

    val WIDE_DEFAULT_SKINS: List<Identifier> = listOf(
        STEVE_SKIN,
        NameSpaces.MINECRAFT.path("textures/entity/player/wide/alex.png"),
        NameSpaces.MINECRAFT.path("textures/entity/player/wide/ari.png"),
        NameSpaces.MINECRAFT.path("textures/entity/player/wide/efe.png"),
        NameSpaces.MINECRAFT.path("textures/entity/player/wide/kai.png"),
        NameSpaces.MINECRAFT.path("textures/entity/player/wide/makena.png"),
        NameSpaces.MINECRAFT.path("textures/entity/player/wide/noor.png"),
        NameSpaces.MINECRAFT.path("textures/entity/player/wide/sunny.png"),
        NameSpaces.MINECRAFT.path("textures/entity/player/wide/zuri.png"),
    )

    val SLIM_DEFAULT_SKINS: List<Identifier> = listOf(
        ALEX_SKIN,
        NameSpaces.MINECRAFT.path("textures/entity/player/slim/steve.png"),
        NameSpaces.MINECRAFT.path("textures/entity/player/slim/ari.png"),
        NameSpaces.MINECRAFT.path("textures/entity/player/slim/efe.png"),
        NameSpaces.MINECRAFT.path("textures/entity/player/slim/kai.png"),
        NameSpaces.MINECRAFT.path("textures/entity/player/slim/makena.png"),
        NameSpaces.MINECRAFT.path("textures/entity/player/slim/noor.png"),
        NameSpaces.MINECRAFT.path("textures/entity/player/slim/sunny.png"),
        NameSpaces.MINECRAFT.path("textures/entity/player/slim/zuri.png"),
    )

    init {
        SpecialPlayerRenderEvent.EVENT.register(::onRenderTexture)
    }

    private fun onRenderTexture(event: SpecialPlayerRenderEvent): EventResult {
        if (!YesSteveModel.isAvailable) return EventResult.pass()
        val modelId = event.modelId ?: return EventResult.pass()
        if (isCustomSkinModel(modelId) || getUseMcDefaultTexture(modelId) > 0) {
            val location = getPlayerSkinLocation(event.player, modelId, event.skin)
            if (location != null) {
                event.textureLocation = location
            }
        }
        return EventResult.pass()
    }

    fun isCustomSkinModel(modelId: String?): Boolean =
        modelId != null && ClientModelManager.isCustomSkinModel(modelId)

    fun isDefaultSkin(location: Identifier?): Boolean =
        location != null && (
                location in WIDE_DEFAULT_SKINS ||
                        location in SLIM_DEFAULT_SKINS
                )

    fun getUseMcDefaultTexture(modelId: String?): Int {
        if (modelId == null) return 0
        val clientVal = ClientModelManager.getUseMcDefaultTexture(modelId)
        if (clientVal != 0) return clientVal
        return ServerModelManager.getUseMcDefaultTexture(modelId)
    }

    fun getDefaultSkinTexture(useMcDefaultTexture: Int, player: Player? = null): Identifier? {
        val targetPlayer = if (PlayerPreviewEntity.isPreviewPlayer(player) || player == null)
            runCatching { Minecraft.getInstance().player }.getOrNull() ?: player else player

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

    fun getSkinTexture(str: String): Identifier? {
        val defaultType = getUseMcDefaultTexture(str)
        val defaultTex = getDefaultSkinTexture(defaultType)
        return defaultTex
    }

    fun getPlayerSkinLocation(player: Player?, modelId: String? = null, skinOverride: PlayerSkin? = null): Identifier? {
        val targetPlayer = if (PlayerPreviewEntity.isPreviewPlayer(player) || player == null) {
            runCatching { Minecraft.getInstance().player }.getOrNull() ?: player
        } else {
            player
        }

        val isCustomSkin = isCustomSkinModel(modelId)

        if (isCustomSkin) {
            val directLoc = runCatching { skinOverride?.body()?.texturePath() }.getOrNull()
            if (directLoc != null && !isDefaultSkin(directLoc)) return directLoc

            val loc = runCatching { (targetPlayer as? AbstractClientPlayer)?.skin?.body()?.texturePath() }.getOrNull()
            if (loc != null && !isDefaultSkin(loc)) return loc

            if (targetPlayer != null) {
                val infoLoc = runCatching {
                    Minecraft.getInstance().connection?.getPlayerInfo(targetPlayer.uuid)?.skin?.body()?.texturePath()
                }.getOrNull()
                if (infoLoc != null && !isDefaultSkin(infoLoc)) return infoLoc

                val lookupLoc = runCatching {
                    Minecraft.getInstance().skinManager.createLookup(targetPlayer.gameProfile, false).get().body()
                        .texturePath()
                }.getOrNull()
                if (lookupLoc != null && !isDefaultSkin(lookupLoc)) return lookupLoc

                val futureLoc = runCatching {
                    Minecraft.getInstance().skinManager.get(targetPlayer.gameProfile).getNow(null)?.getOrNull()?.body()
                        ?.texturePath()
                }.getOrNull()
                if (futureLoc != null && !isDefaultSkin(futureLoc)) return futureLoc
            }
        }

        if (modelId != null) {
            val defaultType = getUseMcDefaultTexture(modelId)
            if (defaultType > 0) return getDefaultSkinTexture(defaultType, targetPlayer)
            if (isCustomSkin) return null
            return getSkinTexture(modelId)
        }
        return null
    }
}
