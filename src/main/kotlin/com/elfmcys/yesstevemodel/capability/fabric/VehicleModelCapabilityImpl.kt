package com.elfmcys.yesstevemodel.capability.fabric

import com.elfmcys.yesstevemodel.capability.VehicleModelCapability
import com.elfmcys.yesstevemodel.fabric.YsmComponents
import net.minecraft.world.entity.Entity

object VehicleModelCapabilityImpl {
    @JvmStatic
    operator fun get(entity: Entity): VehicleModelCapability? {
        val component = YsmComponents.VEHICLE_MODEL.getNullable(entity)
        return component?.capability
    }
}
