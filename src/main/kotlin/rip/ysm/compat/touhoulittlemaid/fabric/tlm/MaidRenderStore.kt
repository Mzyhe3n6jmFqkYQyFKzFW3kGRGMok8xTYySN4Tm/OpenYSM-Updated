package rip.ysm.compat.touhoulittlemaid.fabric.tlm

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.world.entity.Entity
import java.util.Map
import java.util.Optional
import java.util.WeakHashMap

class MaidRenderStore {
    constructor() {
    }
    companion object {
        @JvmField var CACHE: MutableMap<EntityMaid, MaidAnimatable> = WeakHashMap()
        @JvmStatic fun getOrCreate(maid: EntityMaid): MaidAnimatable {
            CACHE.computeIfAbsent(maid, { m -> 
MaidAnimatable(m, true)
 })
        }
        @JvmStatic fun get(entity: Entity): Optional<MaidAnimatable> {
            if (entity is EntityMaid) {
                Optional.ofNullable(CACHE.get(maid))
            }
            return Optional.empty()
        }
        @JvmStatic fun clear() {
            CACHE.clear()
        }
    }
}