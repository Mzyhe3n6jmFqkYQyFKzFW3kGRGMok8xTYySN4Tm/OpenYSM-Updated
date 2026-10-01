package rip.ysm.compat.gun.common

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import net.minecraft.world.InteractionHand
import rip.ysm.compat.gun.swarfare.SWarfareCompat
import rip.ysm.compat.gun.tacz.TacCompat

class ItemUseAnimationPredicate : IAnimationPredicate<LivingAnimatable<*>> {
    override fun predicate(event: AnimationEvent<LivingAnimatable<*>>, evaluator: ExpressionEvaluator<*>?): PlayState {
        val animatable = event.animatable
        val livingEntity = animatable?.entity
        if (livingEntity == null || animatable is IPreviewAnimatable) {
            return PlayState.STOP
        }
        if (!livingEntity.swinging && !livingEntity.isUsingItem) {
            val itemInHand = livingEntity.getItemInHand(InteractionHand.MAIN_HAND)
            var playState = TacCompat.handleGunActionAnimState(itemInHand, event)
            if (playState == null) {
                playState = SWarfareCompat.handleGunActionAnim(itemInHand, event)
            }
            return playState ?: PlayState.STOP
        }
        return PlayState.STOP
    }

    companion object {
        @JvmStatic
        val isModLoaded by lazy { TacCompat.isModLoaded || SWarfareCompat.isModLoaded }
    }
}
