package rip.ysm.compat.touhoulittlemaid

import net.minecraft.world.entity.Entity
import rip.ysm.compat.touhoulittlemaid.fabric.MaidCapabilityBridgeImpl
import java.util.Optional

object MaidCapabilityBridge {
    @JvmStatic
    fun get(entity: Entity): Optional<Any> = MaidCapabilityBridgeImpl.get(entity)
}
