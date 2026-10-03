package rip.ysm.compat.touhoulittlemaid.fabric.tlm.anim

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidGameRecordManager
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.world.entity.LivingEntity
import rip.ysm.compat.touhoulittlemaid.fabric.tlm.MaidAnimatable

@Environment(EnvType.CLIENT)
open class MaidGameStateAnimationPredicate : IAnimationPredicate<MaidAnimatable> {

    override fun predicate(event: AnimationEvent<MaidAnimatable>, evaluator: ExpressionEvaluator<*>?): PlayState {
        val maid: EntityMaid = event.getAnimatable().entity
        if (maid == null || event.getAnimatable() is IPreviewAnimatable) {
            return PlayState.STOP
        }
        if ((maid as LivingEntity).vehicle is EntitySit) {
            val gameRecordManager: MaidGameRecordManager = maid.gameRecordManager
            if (gameRecordManager.isWin) {
                return IAnimationPredicate.playLoopAnimation(event, "game_win")
            }
            if (gameRecordManager.isLost) {
                return IAnimationPredicate.playLoopAnimation(event, "game_lost")
            }
        }
        if (maid.isBegging) {
            return IAnimationPredicate.playLoopAnimation(event, "beg")
        }
        return PlayState.STOP
    }

    companion object {
        @JvmField
        val GAME_STATE_ANIMATIONS: Array<String> = arrayOf("game_win", "game_lost", "beg")
    }
}
