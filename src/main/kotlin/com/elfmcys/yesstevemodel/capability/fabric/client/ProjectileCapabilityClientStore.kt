package com.elfmcys.yesstevemodel.capability.fabric.client

import com.elfmcys.yesstevemodel.capability.ProjectileCapability
import net.minecraft.world.entity.projectile.Projectile
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

object ProjectileCapabilityClientStore {
    val STORE: ConcurrentMap<UUID, ProjectileCapability> = ConcurrentHashMap()

    operator fun get(projectile: Projectile): ProjectileCapability? =
        STORE.computeIfAbsent(projectile.uuid) { ProjectileCapability(projectile) }

    fun clear() = STORE.clear()
}
