package com.elfmcys.yesstevemodel.client.entity

import com.elfmcys.yesstevemodel.client.animation.AnimationTracker

interface IPreviewAnimatable {
    fun getAnimationStateMachine(): AnimationTracker
    fun setCustomAnimationActive(active: Boolean)
}