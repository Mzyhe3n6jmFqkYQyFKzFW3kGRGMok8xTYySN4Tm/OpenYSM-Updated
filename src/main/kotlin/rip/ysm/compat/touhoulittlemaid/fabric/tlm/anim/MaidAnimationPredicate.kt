package rip.ysm.compat.touhoulittlemaid.fabric.tlm.anim

import com.elfmcys.yesstevemodel.client.animation.AnimationState
import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import com.github.tartaricacid.touhoulittlemaid.api.client.render.MaidRenderState
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.world.entity.LivingEntity
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidAnimatable

@Environment(EnvType.CLIENT)
open class MaidAnimationPredicate : IAnimationPredicate<MaidAnimatable> {
    override fun predicate(event: AnimationEvent<MaidAnimatable>, evaluator: ExpressionEvaluator<*>?): PlayState {
        val entity: EntityMaid = event.animatable.entity
        if (event.animatable is IPreviewAnimatable) return PlayState.STOP
        if (entity.renderState != MaidRenderState.ENTITY) return PlayState.STOP
        val vehicle = (entity as LivingEntity).vehicle
        if (vehicle != null && vehicle.isAlive) return PlayState.STOP
        for (priority in 0 until PRIORITY_BUCKETS) {
            for (animationState in PRIORITY_HANDLERS[priority]) {
                if (animationState.predicate.test(entity, event)) {
                    val name: String = animationState.animationName
                    val loopType: ILoopType = animationState.loopType
                    return IAnimationPredicate.playAnimationWithLoop(event, name, loopType)
                }
            }
        }
        return PlayState.STOP
    }

    companion object {
        private const val PRIORITY_BUCKETS: Int = 5

        private val PRIORITY_HANDLERS: Array<ReferenceArrayList<AnimationState<EntityMaid, MaidAnimatable>>> =
            Array(PRIORITY_BUCKETS) { ReferenceArrayList(6) }

        @JvmStatic
        fun registerHandler(animationState: AnimationState<EntityMaid, MaidAnimatable>) {
            PRIORITY_HANDLERS[animationState.priority].add(animationState)
        }
    }
}
