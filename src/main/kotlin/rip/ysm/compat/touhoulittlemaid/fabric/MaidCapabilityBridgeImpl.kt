package rip.ysm.compat.touhoulittlemaid.fabric

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import net.minecraft.world.entity.Entity
import rip.ysm.compat.ModCompat

object MaidCapabilityBridgeImpl : ModCompat("touhou_little_maid") {
    @JvmStatic
    fun get(entity: Entity): Any? {
        return entity as? EntityMaid
    }
}
