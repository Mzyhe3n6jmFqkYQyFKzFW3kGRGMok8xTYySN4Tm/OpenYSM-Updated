package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm

import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.ContextFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function.ArgumentCollection
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.arrow.Arrow
import net.minecraft.world.item.alchemy.PotionContents

class EffectLevel : ContextFunction<Entity>() {
    override fun validateArgumentSize(size: Int): Boolean {
        return size >= 1
    }

    override fun eval(context: ExecutionContext<IContext<Entity>>, arguments: ArgumentCollection): Any? {
        var effects: Int = 0
        for (i in 0 until arguments.size()) {
            val effectId: Identifier? = arguments.getResourceLocation(context, i)
            if (effectId != null) {
                val mobEffectHolder: Holder<MobEffect>? =
                    BuiltInRegistries.MOB_EFFECT.get(ResourceKey.create(Registries.MOB_EFFECT, effectId)).orElse(null)
                if (mobEffectHolder != null) {
                    val geoInstance = context.entity.geoInstance
                    if (geoInstance is PlayerCapability && !geoInstance.isLocalPlayerModel) {
                        effects += geoInstance.positionTracker.getEffectAmplifier(mobEffectHolder)
                    } else {
                        when (val entity = context.entity.entity) {
                            is LivingEntity -> {
                                val mobEffectInstance: MobEffectInstance? = entity.getEffect(mobEffectHolder)
                                if (mobEffectInstance != null) {
                                    effects += mobEffectInstance.amplifier + 1
                                }
                            }

                            is Arrow -> {
                                val potionContents: PotionContents =
                                    entity.pickupItemStackOrigin.getOrDefault(
                                        DataComponents.POTION_CONTENTS,
                                        PotionContents.EMPTY
                                    )
                                for (mobEffectInstance in potionContents.allEffects) {
                                    if (mobEffectInstance.effect == mobEffectHolder) {
                                        effects += mobEffectInstance.amplifier + 1
                                        break
                                    }
                                }
                            }

                            else -> {
                                return null
                            }
                        }
                    }
                }
            }
        }
        return effects
    }
}