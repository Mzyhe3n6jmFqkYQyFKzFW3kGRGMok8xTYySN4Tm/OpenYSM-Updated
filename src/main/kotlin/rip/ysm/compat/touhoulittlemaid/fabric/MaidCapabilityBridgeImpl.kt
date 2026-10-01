package rip.ysm.compat.touhoulittlemaid.fabric

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import net.minecraft.world.entity.Entity
import java.util.*

object MaidCapabilityBridgeImpl {
    @JvmStatic
    fun get(entity: Entity): Optional<Any> {
        return if (entity is EntityMaid) {
            Optional.of(entity)
        } else {
            Optional.empty()
        }
    }
}
