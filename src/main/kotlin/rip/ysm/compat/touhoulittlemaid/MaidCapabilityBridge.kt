package rip.ysm.compat.touhoulittlemaid

import net.minecraft.world.entity.Entity
import rip.ysm.compat.ModCompat
import rip.ysm.compat.touhoulittlemaid.fabric.MaidCapabilityBridgeImpl
import java.util.*

object MaidCapabilityBridge : ModCompat("touhou_little_maid") {
    @JvmStatic
    operator fun get(entity: Entity): Optional<Any> {
        if (!isModLoaded) return Optional.empty()
        return MaidCapabilityBridgeImpl.get(entity)
    }
}
