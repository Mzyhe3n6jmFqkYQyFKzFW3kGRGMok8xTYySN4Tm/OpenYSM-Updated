@file:Suppress("unused")

package com.elfmcys.yesstevemodel.client.animation

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool

class AnimationTracker {
    var currentAnimation: String = StringPool.EMPTY
    var previousAnimation: String = StringPool.EMPTY
    var queuedAnimation: String = StringPool.EMPTY
    val hasAnimation: Boolean
        get() = currentAnimation.isNotBlank()

    fun isCurrentAnimation(animationName: String): Boolean =
        hasAnimation && animationName == currentAnimation
}