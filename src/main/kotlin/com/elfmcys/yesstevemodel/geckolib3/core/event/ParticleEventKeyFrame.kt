package com.elfmcys.yesstevemodel.geckolib3.core.event

import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.event.EventKeyFrame

open class ParticleEventKeyFrame(
    startTick: Double,
    eventData: String,
    @JvmField val effect: String,
    @JvmField val locator: String,
    @JvmField val script: String
) : EventKeyFrame<String>(startTick, eventData)