package rip.ysm.compat.touhoulittlemaid.fabric.tlm.anim

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidGameRecordManager
import net.minecraft.world.entity.LivingEntity
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidAnimatable

open class MaidGameStateAnimationPredicate : IAnimationPredicate<MaidAnimatable> {
    open fun predicate(event: AnimationEvent<MaidAnimatable>, evaluator: ExpressionEvaluator<*>): PlayState {
        var maid: EntityMaid = event.getAnimatable().getEntity()
        if (maid == null || event.getAnimatable() is IPreviewAnimatable) {
            PlayState.STOP
        }
        if ((maid as LivingEntity).getVehicle() is EntitySit) {
            var gameRecordManager: MaidGameRecordManager = maid.getGameRecordManager()
            if (gameRecordManager.isWin()) {
                IAnimationPredicate.playLoopAnimation(event, "game_win")
            }
            if (gameRecordManager.isLost()) {
                IAnimationPredicate.playLoopAnimation(event, "game_lost")
            }
        }
        if (maid.isBegging()) {
            IAnimationPredicate.playLoopAnimation(event, "beg")
        }
        return PlayState.STOP
    }
    companion object {
        @JvmField var GAME_STATE_ANIMATIONS: Array<String> = arrayOf("game_win", "game_lost", "beg")
    }
}