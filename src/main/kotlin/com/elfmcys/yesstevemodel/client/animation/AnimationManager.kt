package com.elfmcys.yesstevemodel.client.animation

import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import net.minecraft.world.entity.player.Player
import rip.ysm.compat.create.CreateCompat
import rip.ysm.compat.gun.swarfare.SWarfareCompat
import rip.ysm.compat.gun.tacz.TacCompat
import rip.ysm.compat.parcool.ParcoolCompat
import rip.ysm.compat.slashblade.SlashBladeCompat

class AnimationManager : IAnimationPredicate<CustomPlayerEntity> {
    companion object {
        @JvmField
        val data: Array<ReferenceArrayList<AnimationState<Player, CustomPlayerEntity>>> =
            Array(Priority.LOWEST + 1) { ReferenceArrayList(6) }

        @JvmStatic
        fun register(state: AnimationState<Player, CustomPlayerEntity>) {
            data[state.priority].add(state)
        }
    }

    override fun predicate(event: AnimationEvent<CustomPlayerEntity>, evaluator: ExpressionEvaluator<*>?): PlayState {
        val animatable = event.animatable
        val player = animatable.entity
        if (animatable is IPreviewAnimatable) return PlayState.STOP
        if (ParcoolCompat.isPlayerParcooling(player)) return PlayState.STOP
        val vehicle = player.vehicle
        if (vehicle != null && vehicle.isAlive) return PlayState.STOP
        if (CreateCompat.isPlayerOnCreateContraption(player))
            return IAnimationPredicate.predicate(event, "parcool:ride_zipline")
        for (i in Priority.HIGHEST..Priority.LOWEST) {
            for (animationState in data[i]) {
                if (animationState.predicate.test(player, event)) {
                    val name = animationState.animationName
                    val loopType = animationState.loopType
                    val slashBladePlayState = SlashBladeCompat.handleSlashBladeAnim(player, event, name, loopType)
                    if (slashBladePlayState != null) return slashBladePlayState
                    var taczPlayState = TacCompat.handleTaczAnimState(player, event, name, loopType)
                    if (taczPlayState == null)
                        taczPlayState = SWarfareCompat.handleTaczAnim(player, event, name, loopType)
                    return taczPlayState ?: IAnimationPredicate.playAnimationWithLoop(event, name, loopType)
                }
            }
        }
        return PlayState.STOP
    }
}