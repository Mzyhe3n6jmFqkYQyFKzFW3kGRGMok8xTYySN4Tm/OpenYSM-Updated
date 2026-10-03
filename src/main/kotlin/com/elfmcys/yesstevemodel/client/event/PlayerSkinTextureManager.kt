@file:Suppress("MemberVisibilityCanBePrivate")

package com.elfmcys.yesstevemodel.client.event

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.event.api.SpecialPlayerRenderEvent
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.Minecraft
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.resources.Identifier
import rip.ysm.api.event.EventResult

@Environment(EnvType.CLIENT)
object PlayerSkinTextureManager {
    @JvmField
    val STEVE_SKIN: Identifier = Identifier.parse("textures/entity/player/wide/steve.png")

    @JvmField
    val ALEX_SKIN: Identifier = Identifier.parse("textures/entity/player/slim/alex.png")

    private const val STEVE_TEXTURE_ID: String = "misc/2_steve"
    private const val ALEX_TEXTURE_ID: String = "misc/1_alex"

    init {
        SpecialPlayerRenderEvent.EVENT.register(::onRenderTexture)
    }

    private fun onRenderTexture(event: SpecialPlayerRenderEvent): EventResult {
        if (!YesSteveModel.isAvailable()) {
            return EventResult.pass()
        }
        val player = event.player
        val modelId = event.modelId
        if (modelId != null && isDefaultSkin(modelId) && player is AbstractClientPlayer) {
            val minecraft = Minecraft.getInstance()
            val skinLookup = minecraft.skinManager.createLookup(player.gameProfile, false)
            val skin = skinLookup.get()
            val location = skin.body().texturePath()
            event.textureLocation = location
        }
        return EventResult.pass()
    }

    fun isDefaultSkin(str: String): Boolean = str == STEVE_TEXTURE_ID || str == ALEX_TEXTURE_ID

    @Suppress("unused")
    fun getSkinTexture(str: String): Identifier {
        return if (str == STEVE_TEXTURE_ID) STEVE_SKIN else ALEX_SKIN
    }
}
