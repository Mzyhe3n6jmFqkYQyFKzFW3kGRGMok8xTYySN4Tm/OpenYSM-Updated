package rip.ysm.api.entity

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.Entity
import rip.ysm.api.entity.fabric.EntityDataBridgeImpl

object EntityDataBridge {
    @JvmStatic
    fun getPersistentData(entity: Entity): CompoundTag {
        return EntityDataBridgeImpl.getPersistentData(entity)
    }

    @JvmStatic
    fun shouldRiderSit(vehicle: Entity): Boolean {
        return EntityDataBridgeImpl.shouldRiderSit(vehicle)
    }
}
