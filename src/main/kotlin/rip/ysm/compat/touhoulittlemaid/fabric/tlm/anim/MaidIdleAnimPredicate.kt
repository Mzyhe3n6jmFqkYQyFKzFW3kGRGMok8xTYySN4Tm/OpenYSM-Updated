package rip.ysm.compat.touhoulittlemaid.fabric.tlm.anim

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidAnimatable

@Environment(EnvType.CLIENT)
open class MaidIdleAnimPredicate : IAnimationPredicate<MaidAnimatable> {
    override fun predicate(event: AnimationEvent<MaidAnimatable>, evaluator: ExpressionEvaluator<*>?): PlayState {
        val animatable: MaidAnimatable = event.animatable
        if (animatable is IPreviewAnimatable) {
            val previewAnimatable = animatable as IPreviewAnimatable
            if (previewAnimatable.animationStateMachine.hasAnimation())
                return IAnimationPredicate.playLoopAnimation(
                    event,
                    previewAnimatable.animationStateMachine.currentAnimation
                )
            return PlayState.STOP
        }
        if (animatable.isModelAvailable()) {
            if (animatable.hasModel()) {
                animatable.refreshModel()
                event.controller?.stopTransition()
            }
            return IAnimationPredicate.predicate(event, animatable.getModelTextureId())
        }
        return PlayState.STOP
    }
}
