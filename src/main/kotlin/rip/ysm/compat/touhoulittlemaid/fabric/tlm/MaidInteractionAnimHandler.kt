package rip.ysm.compat.touhoulittlemaid.fabric.tlm

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.github.tartaricacid.touhoulittlemaid.entity.favorability.Type
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityBroom
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityChair
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity

object MaidInteractionAnimHandler {

    @JvmStatic
    fun handleMaidInteractionAnim(
        event: AnimationEvent<LivingAnimatable<*>>,
        livingEntity: LivingEntity,
        entity: Entity
    ): PlayState? {
        if (event.animatable is IPreviewAnimatable) {
            return null
        }
        if (entity is EntitySit) {
            val joyType = entity.joyType
            if (joyType == Type.GOMOKU.typeName) {
                return IAnimationPredicate.playLoopAnimation(event, "gomoku")
            }
            if (joyType == Type.BOOKSHELF.typeName) {
                return IAnimationPredicate.playLoopAnimation(event, "bookshelf")
            }
            if (joyType == Type.COMPUTER.typeName) {
                return IAnimationPredicate.playLoopAnimation(event, "computer")
            }
            if (joyType == Type.KEYBOARD.typeName) {
                return IAnimationPredicate.playLoopAnimation(event, "keyboard")
            }
            if (joyType == Type.ON_HOME_MEAL.typeName) {
                return IAnimationPredicate.playLoopAnimation(event, "picnic")
            }
        }
        if (entity is EntityChair) {
            return IAnimationPredicate.playLoopAnimation(event, "chair")
        }
        if (entity is EntityBroom) {
            return IAnimationPredicate.playLoopAnimation(event, "broom")
        }
        return null
    }
}
