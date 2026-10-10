package rip.ysm.api.entity.fabric

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.Entity
import java.util.*

object EntityDataBridgeImpl {
    private val PERSISTENT_DATA: MutableMap<Entity, CompoundTag> = Collections.synchronizedMap(WeakHashMap())

    fun getPersistentData(entity: Entity): CompoundTag {
        return PERSISTENT_DATA.computeIfAbsent(entity) { CompoundTag() }
    }

    fun shouldRiderSit(vehicle: Entity): Boolean {
        return true
    }
}
