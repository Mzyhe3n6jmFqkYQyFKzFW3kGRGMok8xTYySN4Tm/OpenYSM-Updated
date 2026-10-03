@file:Suppress("unused")

package com.elfmcys.yesstevemodel.geckolib3.core.keyframe.event

open class EventKeyFrame<T>(startTick: Double, open val eventData: T) {
    open val startTick: Float = startTick.toFloat()
}