package com.elfmcys.yesstevemodel.client.animation

import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent

object AnimationFormatValidator {
    fun validate(event: AnimationEvent<out AnimatableEntity<*>>, animationName: String, version: Int): Boolean {
        if (version >= 19) return true
        val animation = event.animatable.getAnimation(animationName) ?: return false
        return animation.isFromPrimaryAssembly
    }
}