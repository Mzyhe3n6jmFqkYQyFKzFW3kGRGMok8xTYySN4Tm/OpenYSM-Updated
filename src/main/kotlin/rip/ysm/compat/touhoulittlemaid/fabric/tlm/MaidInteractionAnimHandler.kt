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
import org.jetbrains.annotations.Nullable

class MaidInteractionAnimHandler {
    constructor() {
    }
    companion object {
        @JvmStatic fun handleMaidInteractionAnim(event: AnimationEvent<LivingAnimatable<*>>, livingEntity: LivingEntity, entity: Entity): PlayState {
            if (event.getAnimatable() is IPreviewAnimatable) {
                null
            }
            if (entity is EntitySit) {
                var joyType: String = sit.getJoyType()
                if (joyType.equals(Type.GOMOKU.getTypeName())) {
                    IAnimationPredicate.playLoopAnimation(event, "gomoku")
                }
                if (joyType.equals(Type.BOOKSHELF.getTypeName())) {
                    IAnimationPredicate.playLoopAnimation(event, "bookshelf")
                }
                if (joyType.equals(Type.COMPUTER.getTypeName())) {
                    IAnimationPredicate.playLoopAnimation(event, "computer")
                }
                if (joyType.equals(Type.KEYBOARD.getTypeName())) {
                    IAnimationPredicate.playLoopAnimation(event, "keyboard")
                }
                if (joyType.equals(Type.ON_HOME_MEAL.getTypeName())) {
                    IAnimationPredicate.playLoopAnimation(event, "picnic")
                }
            }
            if (entity is EntityChair) {
                IAnimationPredicate.playLoopAnimation(event, "chair")
            }
            if (entity is EntityBroom) {
                IAnimationPredicate.playLoopAnimation(event, "broom")
            }
            return null
        }
    }
}