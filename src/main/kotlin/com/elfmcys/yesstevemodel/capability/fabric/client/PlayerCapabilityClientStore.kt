package com.elfmcys.yesstevemodel.capability.fabric.client

import com.elfmcys.yesstevemodel.capability.PlayerCapability
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.world.entity.player.Player
import rip.ysm.api.capability.CapabilityLifecycle
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

object PlayerCapabilityClientStore {
    @JvmField
    val STORE: ConcurrentMap<UUID, PlayerCapability> = ConcurrentHashMap()

    @JvmStatic
    operator fun get(player: Player): PlayerCapability? {
        if (player !is AbstractClientPlayer) return null
        val uuid = player.uuid
        val existing = STORE[uuid]
        if (existing != null && existing.entity == player) return existing
        val fresh = PlayerCapability(player)
        STORE[uuid] = fresh
        return fresh
    }

    @JvmStatic
    fun clear() = STORE.clear()
}
