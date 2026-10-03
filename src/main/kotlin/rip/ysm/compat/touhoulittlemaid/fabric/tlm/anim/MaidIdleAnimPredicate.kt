package rip.ysm.compat.touhoulittlemaid.fabric.tlm.anim

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidAnimatable

open class MaidIdleAnimPredicate : IAnimationPredicate<MaidAnimatable> {
    open fun predicate(event: AnimationEvent<MaidAnimatable>, evaluator: ExpressionEvaluator<*>): PlayState {
        var animatable: MaidAnimatable = event.getAnimatable()
        if (animatable is IPreviewAnimatable) {
            if (previewAnimatable.getAnimationStateMachine().hasAnimation()) {
                IAnimationPredicate.playLoopAnimation(event, previewAnimatable.getAnimationStateMachine().getCurrentAnimation())
            }
            return PlayState.STOP
        }
        if (animatable.isModelAvailable()) {
            if (animatable.hasModel()) {
                animatable.refreshModel()
                event.getController().stopTransition()
            }
            return IAnimationPredicate.predicate(event, animatable.getModelTextureId())
        }
        return PlayState.STOP
    }
}