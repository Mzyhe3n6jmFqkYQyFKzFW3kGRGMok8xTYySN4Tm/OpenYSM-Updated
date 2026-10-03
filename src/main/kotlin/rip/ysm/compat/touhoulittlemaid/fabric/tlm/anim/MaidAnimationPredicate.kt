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
import net.minecraft.world.entity.LivingEntity
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.world.entity.Entity
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidAnimatable

open class MaidAnimationPredicate : IAnimationPredicate<MaidAnimatable> {
    open fun predicate(event: AnimationEvent<MaidAnimatable>, evaluator: ExpressionEvaluator<*>): PlayState {
        var entity: EntityMaid = event.getAnimatable().getEntity()
        if (entity == null || event.getAnimatable() is IPreviewAnimatable) {
            PlayState.STOP
        }
        if (entity.renderState != MaidRenderState.ENTITY) {
            PlayState.STOP
        }
        var vehicle: Entity = (entity as LivingEntity).getVehicle()
        if (vehicle != null && vehicle.isAlive()) {
            PlayState.STOP
        }
        var priority = 0
        while (priority < PRIORITY_BUCKETS) {
            for (animationState in PRIORITY_HANDLERS[priority]) {
                if (animationState.getPredicate().test(entity, event)) {
                    var name: String = animationState.getAnimationName()
                    var loopType: ILoopType = animationState.getLoopType()
                    return IAnimationPredicate.playAnimationWithLoop(event, name, loopType)
                }
            }
            priority++
        }
        return PlayState.STOP
    }
    companion object {
        @JvmField var PRIORITY_BUCKETS: Int = 5
        @JvmField var PRIORITY_HANDLERS: Array<ReferenceArrayList<AnimationState<EntityMaid, MaidAnimatable>>> = arrayOfNulls<ReferenceArrayList>(PRIORITY_BUCKETS)
        @JvmStatic fun registerHandler(animationState: AnimationState<EntityMaid, MaidAnimatable>) {
            PRIORITY_HANDLERS[animationState.getPriority()].add(animationState)
        }
    }
}