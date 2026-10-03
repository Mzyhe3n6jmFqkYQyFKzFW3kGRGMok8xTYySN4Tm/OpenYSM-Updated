package com.elfmcys.yesstevemodel.geckolib3.core.keyframe.event

open class EventKeyFrame<T>(startTick: Double, private val eventData: T) {
    private val startTick: Float = startTick.toFloat()

    open fun getEventData(): T = eventData
    open fun getStartTick(): Float = startTick
}