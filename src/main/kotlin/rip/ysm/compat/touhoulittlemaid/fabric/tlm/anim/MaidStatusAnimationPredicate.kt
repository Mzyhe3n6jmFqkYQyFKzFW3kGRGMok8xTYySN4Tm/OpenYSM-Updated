package rip.ysm.compat.touhoulittlemaid.fabric.tlm.anim

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import com.github.tartaricacid.touhoulittlemaid.api.client.render.MaidRenderState
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidAnimatable

@Environment(EnvType.CLIENT)
open class MaidStatusAnimationPredicate : IAnimationPredicate<MaidAnimatable> {
    override fun predicate(event: AnimationEvent<MaidAnimatable>, evaluator: ExpressionEvaluator<*>?): PlayState {
        val entityMaid = event.animatable.entity
        return when {
            event.animatable is IPreviewAnimatable -> PlayState.STOP
            entityMaid.renderState == MaidRenderState.STATUE -> {
                IAnimationPredicate.playLoopAnimation(event, "statue")
            }

            entityMaid.renderState == MaidRenderState.GARAGE_KIT -> {
                IAnimationPredicate.playLoopAnimation(event, "garage_kit")
            }

            else -> PlayState.STOP
        }
    }

    companion object {
        @JvmField
        val RENDER_STATES: Array<String> = arrayOf("statue", "garage_kit")
    }
}
