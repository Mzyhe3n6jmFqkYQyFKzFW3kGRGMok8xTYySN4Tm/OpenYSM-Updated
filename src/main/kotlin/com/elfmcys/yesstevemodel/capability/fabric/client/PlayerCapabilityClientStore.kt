package com.elfmcys.yesstevemodel.capability.fabric.client

import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.google.common.collect.MapMaker
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.world.entity.player.Player
import rip.ysm.api.capability.CapabilityLifecycle
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object PlayerCapabilityClientStore {
    private val STORE_BY_PLAYER: MutableMap<Player, PlayerCapability> = MapMaker().weakKeys().makeMap()
    private val LAST_BY_UUID: MutableMap<UUID, PlayerCapability> = ConcurrentHashMap()

    @JvmStatic
    operator fun get(player: Player): PlayerCapability? {
        if (player !is AbstractClientPlayer) return null
        val existing = STORE_BY_PLAYER[player]
        if (existing != null) return existing
        val fresh = PlayerCapability(player)
        val previous = LAST_BY_UUID[player.uuid]
        if (previous != null && previous.entity != player) {
            CapabilityLifecycle.revive(previous.entity)
            fresh.copyFrom(previous)
            CapabilityLifecycle.invalidate(previous.entity)
        }
        STORE_BY_PLAYER[player] = fresh
        LAST_BY_UUID[player.uuid] = fresh
        return fresh
    }

    @JvmStatic
    fun clear() {
        STORE_BY_PLAYER.clear()
        LAST_BY_UUID.clear()
    }
}
