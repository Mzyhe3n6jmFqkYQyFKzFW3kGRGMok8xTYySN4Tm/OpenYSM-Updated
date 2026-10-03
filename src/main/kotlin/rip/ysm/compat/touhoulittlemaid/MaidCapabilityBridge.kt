package rip.ysm.compat.touhoulittlemaid

import net.minecraft.world.entity.Entity
import rip.ysm.compat.ModCompat
import rip.ysm.compat.touhoulittlemaid.fabric.MaidCapabilityBridgeImpl

object MaidCapabilityBridge : ModCompat("touhou_little_maid") {
    @JvmStatic
    operator fun get(entity: Entity): Any? {
        if (!isModLoaded) return null
        return MaidCapabilityBridgeImpl.get(entity)
    }
}
