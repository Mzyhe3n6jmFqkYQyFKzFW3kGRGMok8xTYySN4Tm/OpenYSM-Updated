package com.elfmcys.yesstevemodel.client.event

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.capability.ModelInfoCapability
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.entity.LivingEntity

object MobEffectEvent {
    @JvmStatic
    fun onEffectAdded(entity: LivingEntity, effect: Holder<MobEffect>?, amplifier: Int) {
        if (!YesSteveModel.isAvailable() || entity.level().isClientSide) return
        if (entity is ServerPlayer && effect != null) {
            ModelInfoCapability[entity]?.animSync?.syncEffectAdded(entity, effect, amplifier + 1)
        }
    }

    @JvmStatic
    fun onEffectRemoved(entity: LivingEntity, effect: Holder<MobEffect>?) {
        if (!YesSteveModel.isAvailable() || entity.level().isClientSide) return
        if (entity is ServerPlayer && effect != null) {
            ModelInfoCapability[entity]?.animSync?.syncEffectRemoved(entity, effect)
        }
    }
}
