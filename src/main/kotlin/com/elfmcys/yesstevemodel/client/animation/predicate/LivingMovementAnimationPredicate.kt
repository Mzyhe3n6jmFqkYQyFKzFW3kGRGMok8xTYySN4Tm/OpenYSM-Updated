package com.elfmcys.yesstevemodel.client.animation.predicate

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.animal.pig.Pig
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.vehicle.boat.Boat
import rip.ysm.compat.carryon.CarryOnCompat
import rip.ysm.compat.swem.SWEMCompat
import rip.ysm.compat.touhoulittlemaid.TouhouLittleMaidCompat

class LivingMovementAnimationPredicate : IAnimationPredicate<LivingAnimatable<*>> {
    override fun predicate(event: AnimationEvent<LivingAnimatable<*>>, evaluator: ExpressionEvaluator<*>?): PlayState {
        return renderRidingAnimation(event) ?: PlayState.STOP
    }

    private fun renderRidingAnimation(event: AnimationEvent<LivingAnimatable<*>>): PlayState? {
        val animatable = event.animatable
        val livingEntity = animatable.entity
        if (animatable is IPreviewAnimatable) return null
        val vehicle = livingEntity.vehicle ?: return null
        if (!vehicle.isAlive) return null
        val str = SWEMCompat.getHorseGaitName(livingEntity)
        if (str.isNotBlank())
            return IAnimationPredicate.playAnimationWithLoop(event, str, ILoopType.EDefaultLoopTypes.LOOP)
        val conditionManager = animatable.modelConfig ?: return null
        if (TouhouLittleMaidCompat.isModLoaded) {
            val conditionChair = conditionManager.chair
            val str2 = conditionChair.doTest(livingEntity)
            if (str2.isNotBlank())
                return IAnimationPredicate.playAnimationWithLoop(event, str2, ILoopType.EDefaultLoopTypes.LOOP)
        }
        val conditionVehicle = conditionManager.vehicle
        val str3 = conditionVehicle.doTest(livingEntity)
        if (str3.isNotBlank())
            return IAnimationPredicate.playAnimationWithLoop(event, str3, ILoopType.EDefaultLoopTypes.LOOP)
        if (vehicle is Pig)
            return IAnimationPredicate.playAnimationWithLoop(event, "ride_pig", ILoopType.EDefaultLoopTypes.LOOP)
        if (vehicle is Mob && vehicle.isSaddled)
            return IAnimationPredicate.playAnimationWithLoop(event, "ride", ILoopType.EDefaultLoopTypes.LOOP)
        if (vehicle is Boat)
            return IAnimationPredicate.playAnimationWithLoop(event, "boat", ILoopType.EDefaultLoopTypes.LOOP)
        val z = livingEntity is Player && CarryOnCompat.isPlayerCarrying(livingEntity)
        val z2 = TouhouLittleMaidCompat.isMaidEntity(livingEntity) && (livingEntity.vehicle is Player)
        if (z || z2)
            return IAnimationPredicate.playAnimationWithLoop(
                event,
                "carryon:princess",
                ILoopType.EDefaultLoopTypes.LOOP
            )
        val playState = TouhouLittleMaidCompat.handleMaidInteraction(event, livingEntity, vehicle)
        if (playState != null) return playState
        return IAnimationPredicate.playAnimationWithLoop(event, "sit", ILoopType.EDefaultLoopTypes.LOOP)
    }
}