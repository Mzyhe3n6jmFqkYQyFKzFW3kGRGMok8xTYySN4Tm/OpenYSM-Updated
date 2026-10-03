package com.elfmcys.yesstevemodel.client.animation

import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import net.minecraft.util.Mth
import java.util.function.BiPredicate

class AnimationState<TE, AE : AnimatableEntity<*>>(
    val animationName: String,
    val loopType: ILoopType,
    priority: Int,
    val predicate: BiPredicate<TE, AnimationEvent<AE>>
) {
    val priority: Int = Mth.clamp(priority, Priority.HIGHEST, Priority.LOWEST)
}