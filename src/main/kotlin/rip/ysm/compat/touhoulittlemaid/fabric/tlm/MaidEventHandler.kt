package rip.ysm.compat.touhoulittlemaid.fabric.tlm

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityChair
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import com.github.tartaricacid.touhoulittlemaid.item.ItemHakureiGohei
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item

object MaidEventHandler {

    fun isMaid(entity: Entity): Boolean = entity is EntityMaid

    fun isYsmModelMaid(entity: Entity): Boolean = entity is EntityMaid && entity.isYsmModel

    fun isChair(entity: Entity): Boolean = entity is EntityChair

    fun isSit(entity: Entity): Boolean = entity is EntitySit

    fun getChairModelId(entity: Entity): String {
        if (entity is EntityChair) {
            return entity.modelId
        }
        return StringPool.EMPTY
    }

    fun isMaidFishing(livingEntity: LivingEntity): Boolean = livingEntity is EntityMaid

    fun isGohei(item: Item): Boolean = item is ItemHakureiGohei
}
