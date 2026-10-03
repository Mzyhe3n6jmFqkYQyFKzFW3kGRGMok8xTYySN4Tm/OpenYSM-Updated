package com.elfmcys.yesstevemodel.client.animation.predicate

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionArmor
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.client.entity.PlayerGeoEntity
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import rip.ysm.compat.cosmeticarmorreworked.CosmeticArmorHelper

class EquipmentSlotAnimationPredicate(private val slot: EquipmentSlot) : IAnimationPredicate<PlayerGeoEntity> {
    override fun predicate(event: AnimationEvent<PlayerGeoEntity>, evaluator: ExpressionEvaluator<*>?): PlayState {
        val animatable = event.getAnimatable()
        val entity: LivingEntity? = animatable.entity
        if (entity == null || animatable is IPreviewAnimatable) {
            return PlayState.STOP
        }
        if (CosmeticArmorHelper.getArmorItem(entity, slot).isEmpty) {
            return PlayState.STOP
        }
        val conditionArmor: ConditionArmor? = animatable.getArmModelProcessor()?.conditionArmor
        if (conditionArmor != null) {
            val name: String = conditionArmor.doTest(entity, slot)
            if (name.isNotBlank()) {
                return IAnimationPredicate.playAnimationWithLoop(event, name, ILoopType.EDefaultLoopTypes.LOOP)
            }
        }
        val str: String = "${slot.getName()}:default"
        if (animatable.getAnimation(str) != null) {
            return IAnimationPredicate.playAnimationWithLoop(event, str, ILoopType.EDefaultLoopTypes.LOOP)
        }
        return PlayState.STOP
    }
}