package com.elfmcys.yesstevemodel.capability.fabric.client

import com.elfmcys.yesstevemodel.capability.VehicleCapability
import net.minecraft.world.entity.Entity
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

object VehicleCapabilityClientStore {
    val STORE: ConcurrentMap<UUID, VehicleCapability> = ConcurrentHashMap()

    operator fun get(entity: Entity): VehicleCapability? =
        STORE.computeIfAbsent(entity.uuid) { VehicleCapability(entity) }

    fun clear() = STORE.clear()
}
