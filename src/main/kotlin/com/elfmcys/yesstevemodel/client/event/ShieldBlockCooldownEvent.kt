@file:Suppress("unused")

package com.elfmcys.yesstevemodel.client.event

import net.minecraft.world.entity.LivingEntity
import rip.ysm.api.entity.EntityDataBridge
import kotlin.jvm.optionals.getOrNull

object ShieldBlockCooldownEvent {
    private const val TAG_KEY: String = $$"ysm$shield_block_cooldown"

    fun onShieldBlock(entity: LivingEntity) {
        EntityDataBridge.getPersistentData(entity).putInt(TAG_KEY, 5)
    }

    fun onLivingTick(entity: LivingEntity) {
        val tag = EntityDataBridge.getPersistentData(entity)
        val cooldown = tag.getInt(TAG_KEY).getOrNull()
        if (cooldown != null) {
            if (cooldown > 0) {
                tag.putInt(TAG_KEY, cooldown - 1)
            } else {
                tag.remove(TAG_KEY)
            }
        }
    }

    fun isOnCooldown(livingEntity: LivingEntity): Boolean =
        EntityDataBridge.getPersistentData(livingEntity).getInt(TAG_KEY).isPresent
}
