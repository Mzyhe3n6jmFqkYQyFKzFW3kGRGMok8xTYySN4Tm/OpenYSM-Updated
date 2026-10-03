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
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.CrossbowItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import rip.ysm.compat.gun.swarfare.SWarfareCompat
import rip.ysm.compat.gun.tacz.TacCompat
import rip.ysm.compat.touhoulittlemaid.TouhouLittleMaidCompat

class MainHandHoldPredicate : IAnimationPredicate<LivingAnimatable<*>> {
    override fun predicate(event: AnimationEvent<LivingAnimatable<*>>, evaluator: ExpressionEvaluator<*>?): PlayState {
        val animatable = event.getAnimatable()
        val entity: LivingEntity = animatable.entity as? LivingEntity ?: return PlayState.STOP
        if (animatable is IPreviewAnimatable) {
            return PlayState.STOP
        }
        if (!checkSwingAndUse(entity, InteractionHand.MAIN_HAND)) {
            return PlayState.PAUSE
        }
        val i: Int = animatable.getModelAssembly()?.modelData?.formatVersion ?: 0
        val mainHandItem: ItemStack = entity.getItemInHand(InteractionHand.MAIN_HAND)
        val playState: PlayState? = TacCompat.handleGunHoldAnimState(mainHandItem, event)
        if (playState != null) {
            return playState
        }
        val gunPlayState: PlayState? = SWarfareCompat.handleGunHoldAnim(mainHandItem, event)
        if (gunPlayState != null) {
            return gunPlayState
        }
        if (mainHandItem.`is`(Items.CROSSBOW) && CrossbowItem.isCharged(mainHandItem)) {
            return IAnimationPredicate.playAnimationWithValid(event, "hold_mainhand:charged_crossbow", ILoopType.EDefaultLoopTypes.LOOP, i)
        }
        val isFishing: Boolean = entity is Player && entity.fishing != null
        val flag: Boolean = TouhouLittleMaidCompat.isMaidSitting(entity)
        if (isFishing || flag) {
            return IAnimationPredicate.playAnimationWithValid(event, "hold_mainhand:fishing", ILoopType.EDefaultLoopTypes.LOOP, i)
        }
        val frameState: LivingEntityFrameState<*> = animatable.getPositionTracker()
        if (!isSameItem(mainHandItem, frameState, InteractionHand.MAIN_HAND)) {
            frameState.setHandItemsForAnimation(mainHandItem, InteractionHand.MAIN_HAND)
            event.getController()?.stopTransition()
        }
        val conditionHold: ConditionHold? = animatable.getModelConfig()?.holdMainhand
        val str: String? = conditionHold?.doTest(entity, InteractionHand.MAIN_HAND)
        if (!str.isNullOrBlank()) {
            return IAnimationPredicate.playAnimationWithValid(event, str, ILoopType.EDefaultLoopTypes.LOOP, i)
        }
        return PlayState.STOP
    }

    private fun isSameItem(itemStack: ItemStack, frameState: LivingEntityFrameState<*>, hand: InteractionHand): Boolean {
        val preItem: ItemStack = frameState.getHandItemsForAnimation(hand)
        if (preItem.isDamaged) {
            return ItemStack.isSameItem(itemStack, preItem)
        }
        return ItemStack.matches(itemStack, preItem)
    }

    private fun checkSwingAndUse(entity: LivingEntity, hand: InteractionHand): Boolean {
        if (entity.swinging && entity.swingingArm == hand) {
            return false
        }
        return !entity.isUsingItem || entity.usedItemHand != hand
    }
}