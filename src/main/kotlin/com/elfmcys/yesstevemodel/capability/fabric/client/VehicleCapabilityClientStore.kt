package com.elfmcys.yesstevemodel.capability.fabric.client

import com.elfmcys.yesstevemodel.capability.VehicleCapability
import net.minecraft.world.entity.Entity
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

object VehicleCapabilityClientStore {
    @JvmField
    val STORE: ConcurrentMap<UUID, VehicleCapability> = ConcurrentHashMap()

    @JvmStatic
    operator fun get(entity: Entity): VehicleCapability? =
        STORE.computeIfAbsent(entity.uuid) { VehicleCapability(entity) }

    @JvmStatic
    fun clear() = STORE.clear()
}
