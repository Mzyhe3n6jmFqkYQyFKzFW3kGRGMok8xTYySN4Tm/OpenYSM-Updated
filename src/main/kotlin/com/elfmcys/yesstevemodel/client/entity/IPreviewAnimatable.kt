package com.elfmcys.yesstevemodel.client.entity

import com.elfmcys.yesstevemodel.client.animation.AnimationTracker

interface IPreviewAnimatable {
    val animationStateMachine: AnimationTracker
    fun setCustomAnimationActive(active: Boolean)
}