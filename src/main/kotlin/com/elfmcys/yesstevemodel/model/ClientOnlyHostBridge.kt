package com.elfmcys.yesstevemodel.model

import com.elfmcys.yesstevemodel.client.ClientOnlyMode
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.Minecraft
import java.util.*

@Environment(EnvType.CLIENT)
internal object ClientOnlyHostBridge {
    fun isActive(): Boolean = ClientOnlyMode.isActive

    fun isLocalHost(uuid: UUID?): Boolean {
        if (uuid == null) return false
        val mc = Minecraft.getInstance()
        if (uuid == mc.user.profileId) return true
        val player = mc.player
        return player != null && uuid == player.uuid
    }
}
