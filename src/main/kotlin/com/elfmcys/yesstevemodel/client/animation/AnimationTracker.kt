package com.elfmcys.yesstevemodel.client.animation

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool

class AnimationTracker {
    @JvmField
    var currentAnimation: String = StringPool.EMPTY
    @JvmField
    var previousAnimation: String = StringPool.EMPTY
    @JvmField
    var queuedAnimation: String = StringPool.EMPTY

    fun hasAnimation(): Boolean = currentAnimation.isNotBlank()

    fun isCurrentAnimation(animationName: String): Boolean =
        hasAnimation() && animationName == currentAnimation

    fun getCurrentAnimation(): String = currentAnimation
    fun setCurrentAnimation(animationName: String) {
        currentAnimation = animationName
    }

    fun getPreviousAnimation(): String = previousAnimation
    fun setPreviousAnimation(animationName: String) {
        previousAnimation = animationName
    }

    fun getQueuedAnimation(): String = queuedAnimation
    fun setQueuedAnimation(animationName: String) {
        queuedAnimation = animationName
    }
}