package com.elfmcys.yesstevemodel.client.animation

import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.extensions.isCrawl
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.player.Player
import java.util.function.BiPredicate
import kotlin.math.abs

object AnimationRegister {
    private const val MIN_SPEED: Float = 0.05f

    init {
        register("death", ILoopType.EDefaultLoopTypes.PLAY_ONCE, Priority.HIGHEST) { player, _ -> player.isDeadOrDying }
        register("riptide", Priority.HIGHEST) { player, _ -> player.isAutoSpinAttack }
        register("sleep", Priority.HIGHEST) { player, _ -> player.hasPose(Pose.SLEEPING) }
        register("swim", Priority.HIGHEST) { player, _ -> player.isSwimming }
        register(
            "climb",
            Priority.HIGHEST
        ) { player, event -> player.isCrawl && abs(event.limbSwingAmount) > MIN_SPEED }
        register("climbing", Priority.HIGHEST) { player, _ -> player.isCrawl }
        register("ladder_up", Priority.HIGHEST) { player, _ -> player.onClimbable() && getVerticalSpeed(player) > 0.0f }
        register(
            "ladder_stillness",
            Priority.HIGHEST
        ) { player, _ -> player.onClimbable() && getVerticalSpeed(player) == 0.0f }
        register(
            "ladder_down",
            Priority.HIGHEST
        ) { player, _ -> player.onClimbable() && getVerticalSpeed(player) < 0.0f }
        register("fly", Priority.HIGH) { player, event ->
            val animatable = event.animatable
            if (animatable is PlayerCapability) {
                if (!animatable.isLocalPlayerModel) {
                    return@register animatable.positionTracker.isFlying
                }
            }
            player.abilities.flying
        }
        register("elytra_fly", Priority.HIGH) { player, _ -> player.hasPose(Pose.FALL_FLYING) && player.isFallFlying }
        register("swim_stand", Priority.NORMAL) { player, _ -> player.isInWater && !player.onGround() }
        register("attacked", ILoopType.EDefaultLoopTypes.PLAY_ONCE, 2) { player, _ -> player.hurtTime > 0 }
        register("jump", Priority.NORMAL) { player, _ -> !player.onGround() && !player.isInWater }
        register("sneak", Priority.NORMAL) { player, event ->
            player.onGround() && player.hasPose(Pose.CROUCHING) && abs(
                event.limbSwingAmount
            ) > MIN_SPEED
        }
        register("sneaking", Priority.NORMAL) { player, _ -> player.onGround() && player.hasPose(Pose.CROUCHING) }
        register("run", Priority.LOW) { player, _ -> player.onGround() && player.isSprinting }
        register("walk", Priority.LOW) { player, event -> player.onGround() && event.limbSwingAmount > MIN_SPEED }
        register("idle", Priority.LOWEST) { _, _ -> true }
    }

    fun register(
        animationName: String,
        loopType: ILoopType,
        priority: Int,
        predicate: BiPredicate<Player, AnimationEvent<CustomPlayerEntity>>
    ) = AnimationManager.register(AnimationState(animationName, loopType, priority, predicate))

    fun register(
        animationName: String,
        priority: Int,
        predicate: BiPredicate<Player, AnimationEvent<CustomPlayerEntity>>
    ) = register(animationName, ILoopType.EDefaultLoopTypes.LOOP, priority, predicate)

    fun getVerticalSpeed(player: Player): Float = 20.0f * (player.position().y - player.yo).toFloat()
}