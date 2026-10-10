package rip.ysm.compat.touhoulittlemaid.fabric.tlm

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.world.entity.Entity
import java.util.*

@Environment(EnvType.CLIENT)
object MaidRenderStore {
    private val CACHE: MutableMap<EntityMaid, MaidAnimatable> = WeakHashMap()

    fun getOrCreate(maid: EntityMaid): MaidAnimatable {
        return CACHE.computeIfAbsent(maid) { m -> MaidAnimatable(m, true) }
    }

    fun get(entity: Entity): MaidAnimatable? {
        if (entity is EntityMaid) {
            return CACHE[entity]
        }
        return null
    }

    fun clear() {
        CACHE.clear()
    }
}
