package com.elfmcys.yesstevemodel.client.animation.predicate

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionHold
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable
import com.elfmcys.yesstevemodel.client.entity.LivingEntityFrameState
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.CrossbowItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class OffHandHoldPredicate : IAnimationPredicate<LivingAnimatable<*>> {
    override fun predicate(event: AnimationEvent<LivingAnimatable<*>>, evaluator: ExpressionEvaluator<*>?): PlayState {
        val animatable = event.animatable
        val entity: LivingEntity = animatable.entity
        if (animatable is IPreviewAnimatable) return PlayState.STOP
        if (!checkSwingAndUse(entity, InteractionHand.OFF_HAND)) return PlayState.PAUSE
        val i: Int = animatable.modelAssembly?.modelData?.formatVersion ?: 0
        val itemInHand: ItemStack = entity.getItemInHand(InteractionHand.OFF_HAND)
        if (itemInHand.`is`(Items.CROSSBOW) && CrossbowItem.isCharged(itemInHand))
            return IAnimationPredicate.playAnimationWithValid(
                event,
                "hold_offhand:charged_crossbow",
                ILoopType.EDefaultLoopTypes.LOOP,
                i
            )
        val frameState: LivingEntityFrameState<*> = animatable.positionTracker
        if (!isSameItem(itemInHand, frameState, InteractionHand.OFF_HAND)) {
            frameState.setHandItemsForAnimation(itemInHand, InteractionHand.OFF_HAND)
            event.controller?.stopTransition()
        }
        val conditionHold: ConditionHold? = animatable.modelConfig?.holdOffhand
        val str: String? = conditionHold?.doTest(entity, InteractionHand.OFF_HAND)
        if (!str.isNullOrBlank()) {
            return IAnimationPredicate.playAnimationWithValid(event, str, ILoopType.EDefaultLoopTypes.LOOP, i)
        }
        return PlayState.STOP
    }

    private fun isSameItem(stack: ItemStack, frameState: LivingEntityFrameState<*>, hand: InteractionHand): Boolean {
        val preItem: ItemStack = frameState.getHandItemsForAnimation(hand)
        if (preItem.isDamaged) return ItemStack.isSameItem(stack, preItem)
        return ItemStack.matches(stack, preItem)
    }

    private fun checkSwingAndUse(entity: LivingEntity, hand: InteractionHand): Boolean =
        !(entity.swinging && entity.swingingArm == hand) && (!entity.isUsingItem || entity.usedItemHand != hand)
}