package rip.ysm.compat.touhoulittlemaid.fabric

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import net.minecraft.world.entity.Entity

object MaidCapabilityBridgeImpl {
    @JvmStatic
    fun get(entity: Entity): Any? {
        return if (entity is EntityMaid) entity else null
    }
}
