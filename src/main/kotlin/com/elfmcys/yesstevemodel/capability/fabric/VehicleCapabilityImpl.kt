package com.elfmcys.yesstevemodel.capability.fabric

import com.elfmcys.yesstevemodel.capability.VehicleCapability
import com.elfmcys.yesstevemodel.capability.fabric.client.VehicleCapabilityClientStore
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.entity.Entity

object VehicleCapabilityImpl {
    @JvmStatic
    operator fun get(entity: Entity): VehicleCapability? {
        if (FabricLoader.getInstance().environmentType != EnvType.CLIENT || !entity.level().isClientSide) return null
        return VehicleCapabilityClientStore[entity]
    }
}
