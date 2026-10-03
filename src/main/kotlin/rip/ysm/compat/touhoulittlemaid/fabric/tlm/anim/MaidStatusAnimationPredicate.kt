package rip.ysm.compat.touhoulittlemaid.fabric.tlm.anim

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import com.github.tartaricacid.touhoulittlemaid.api.client.render.MaidRenderState
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidAnimatable

@Environment(EnvType.CLIENT)
open class MaidStatusAnimationPredicate : IAnimationPredicate<MaidAnimatable> {

    override fun predicate(event: AnimationEvent<MaidAnimatable>, evaluator: ExpressionEvaluator<*>?): PlayState {
        val entityMaid: EntityMaid = event.getAnimatable().entity
        if (entityMaid == null || event.getAnimatable() is IPreviewAnimatable) {
            return PlayState.STOP
        }
        if (entityMaid.renderState == MaidRenderState.STATUE) {
            return IAnimationPredicate.playLoopAnimation(event, "statue")
        }
        if (entityMaid.renderState == MaidRenderState.GARAGE_KIT) {
            return IAnimationPredicate.playLoopAnimation(event, "garage_kit")
        }
        return PlayState.STOP
    }

    companion object {
        @JvmField
        val RENDER_STATES: Array<String> = arrayOf("statue", "garage_kit")
    }
}
