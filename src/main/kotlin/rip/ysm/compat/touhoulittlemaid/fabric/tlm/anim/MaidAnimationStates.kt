package rip.ysm.compat.touhoulittlemaid.fabric.tlm.anim

import com.elfmcys.yesstevemodel.client.animation.AnimationState
import com.elfmcys.yesstevemodel.client.animation.Priority
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Pose
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidAnimatable
import java.util.function.BiPredicate

@Environment(EnvType.CLIENT)
object MaidAnimationStates {
    private const val MOVEMENT_THRESHOLD: Float = 0.05f

    @JvmStatic
    fun register() {
        registerState(
            "death",
            ILoopType.EDefaultLoopTypes.PLAY_ONCE,
            Priority.HIGHEST
        ) { maid, _ -> vanilla(maid).isDeadOrDying }
        registerLoopState("sleep", Priority.HIGHEST) { maid, _ -> vanilla(maid).pose == Pose.SLEEPING }
        registerLoopState("swim", Priority.HIGHEST) { maid, _ -> vanilla(maid).isSwimming }
        registerLoopState("ladder_up", Priority.HIGHEST) { maid, _ ->
            vanilla(maid).onClimbable() && getVerticalSpeed(
                maid
            ) > 0.0f
        }
        registerLoopState(
            "ladder_stillness",
            Priority.HIGHEST
        ) { maid, _ -> vanilla(maid).onClimbable() && getVerticalSpeed(maid) == 0.0f }
        registerLoopState("ladder_down", Priority.HIGHEST) { maid, _ ->
            vanilla(maid).onClimbable() && getVerticalSpeed(
                maid
            ) < 0.0f
        }
        registerLoopState("sit", Priority.HIGH) { maid, _ -> maid.isMaidInSittingPose }
        registerLoopState(
            "swim_stand",
            Priority.NORMAL
        ) { maid, _ -> vanilla(maid).isInWater && !vanilla(maid).onGround() }
        registerState(
            "attacked",
            ILoopType.EDefaultLoopTypes.PLAY_ONCE,
            Priority.NORMAL
        ) { maid, _ -> vanilla(maid).hurtTime > 0 }
        registerLoopState("jump", Priority.NORMAL) { maid, _ -> !vanilla(maid).onGround() && !vanilla(maid).isInWater }
        registerLoopState("run", Priority.LOW) { maid, _ -> vanilla(maid).onGround() && vanilla(maid).isSprinting }
        registerLoopState(
            "walk",
            Priority.LOW
        ) { maid, event -> vanilla(maid).onGround() && event.limbSwingAmount > MOVEMENT_THRESHOLD }
        registerLoopState("idle", Priority.LOWEST) { _, _ -> true }
    }

    private fun registerState(
        name: String,
        loopType: ILoopType,
        priority: Int,
        predicate: BiPredicate<EntityMaid, AnimationEvent<MaidAnimatable>>
    ) {
        MaidAnimationPredicate.registerHandler(AnimationState(name, loopType, priority, predicate))
    }

    private fun registerLoopState(
        name: String,
        priority: Int,
        predicate: BiPredicate<EntityMaid, AnimationEvent<MaidAnimatable>>
    ) {
        registerState(name, ILoopType.EDefaultLoopTypes.LOOP, priority, predicate)
    }

    private fun vanilla(maid: EntityMaid): LivingEntity = maid

    private fun getVerticalSpeed(livingEntity: LivingEntity): Float {
        return 20.0f * (livingEntity.position().y - livingEntity.yo).toFloat()
    }
}
