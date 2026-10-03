package rip.ysm.compat.touhoulittlemaid.fabric.tlm

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityChair
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import com.github.tartaricacid.touhoulittlemaid.item.ItemHakureiGohei
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item

class MaidEventHandler {
    constructor() {
    }
    companion object {
        @JvmStatic fun isMaid(entity: Entity): Boolean {
            entity is EntityMaid
        }
        @JvmStatic fun isYsmModelMaid(entity: Entity): Boolean {
            entity is EntityMaid && maid.isYsmModel()
        }
        @JvmStatic fun isChair(entity: Entity): Boolean {
            entity is EntityChair
        }
        @JvmStatic fun isSit(entity: Entity): Boolean {
            entity is EntitySit
        }
        @JvmStatic fun getChairModelId(entity: Entity): String {
            if (entity is EntityChair) {
                chair.getModelId()
            }
            return StringPool.EMPTY
        }
        @JvmStatic fun isMaidFishing(livingEntity: LivingEntity): Boolean {
            livingEntity is EntityMaid && maid.fishing != null
        }
        @JvmStatic fun isGohei(item: Item): Boolean {
            item is ItemHakureiGohei
        }
    }
}