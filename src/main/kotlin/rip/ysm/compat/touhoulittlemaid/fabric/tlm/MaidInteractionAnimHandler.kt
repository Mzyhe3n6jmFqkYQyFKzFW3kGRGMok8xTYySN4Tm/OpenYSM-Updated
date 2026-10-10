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
    fun handleMaidInteractionAnim(
        event: AnimationEvent<LivingAnimatable<*>>,
        livingEntity: LivingEntity,
        entity: Entity
    ): PlayState? {
        if (event.animatable is IPreviewAnimatable) return null
        return when (entity) {
            is EntitySit -> when (entity.joyType) {
                Type.GOMOKU.typeName -> IAnimationPredicate.playLoopAnimation(event, "gomoku")
                Type.BOOKSHELF.typeName -> IAnimationPredicate.playLoopAnimation(event, "bookshelf")
                Type.COMPUTER.typeName -> IAnimationPredicate.playLoopAnimation(event, "computer")
                Type.KEYBOARD.typeName -> IAnimationPredicate.playLoopAnimation(event, "keyboard")
                Type.ON_HOME_MEAL.typeName -> IAnimationPredicate.playLoopAnimation(event, "picnic")
                else -> null
            }

            is EntityChair -> IAnimationPredicate.playLoopAnimation(event, "chair")
            is EntityBroom -> IAnimationPredicate.playLoopAnimation(event, "broom")
            else -> null
        }
    }
}
