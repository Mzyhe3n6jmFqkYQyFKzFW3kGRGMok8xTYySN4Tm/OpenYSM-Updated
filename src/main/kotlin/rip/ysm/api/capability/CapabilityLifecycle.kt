package rip.ysm.api.capability

import net.minecraft.world.entity.Entity
import rip.ysm.api.capability.fabric.CapabilityLifecycleImpl

object CapabilityLifecycle {
    @JvmStatic
    fun revive(entity: Entity) {
        CapabilityLifecycleImpl.revive(entity)
    }

    @JvmStatic
    fun invalidate(entity: Entity) {
        CapabilityLifecycleImpl.invalidate(entity)
    }
}
