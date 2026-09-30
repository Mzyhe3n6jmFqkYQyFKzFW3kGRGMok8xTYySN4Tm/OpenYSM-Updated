package rip.ysm.compat.touhoulittlemaid.fabric

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import net.minecraft.world.entity.Entity
import rip.ysm.compat.touhoulittlemaid.TouhouMaidCompat
import java.util.Optional

object MaidCapabilityBridgeImpl {
    @JvmStatic
    fun isLoaded(): Boolean = TouhouMaidCompat.isModLoaded

    @JvmStatic
    fun get(entity: Entity): Optional<Any> {
        if (!TouhouMaidCompat.isModLoaded) return Optional.empty()
        return if (entity is EntityMaid) {
            Optional.of(entity)
        } else {
            Optional.empty()
        }
    }
}
