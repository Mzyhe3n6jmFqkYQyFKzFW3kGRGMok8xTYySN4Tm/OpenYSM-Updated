package com.elfmcys.yesstevemodel.geckolib3.core.event

import com.elfmcys.yesstevemodel.audio.AudioPlayerManager
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.event.EventKeyFrame

open class SoundKeyFrameExecutor(
    private val soundKeyFrames: MutableList<EventKeyFrame<String>>,
    private val audioPlayerManager: AudioPlayerManager?
) {
    private var nextIndex: Int = 0

    open fun playSound(entity: AnimatableEntity<*>, currentTick: Float, playAudio: Boolean) {
        while (!reachEnd()) {
            val sound: EventKeyFrame<String> = soundKeyFrames[nextIndex]
            if (sound.startTick > currentTick) {
                return
            }
            nextIndex++
            val eventData = sound.eventData
            if (playAudio && eventData.isNotEmpty()) {
                audioPlayerManager?.playSound(entity, 0, eventData, false, null)
            }
        }
    }

    open fun reset() {
        nextIndex = 0
        audioPlayerManager?.stopAll()
    }

    open fun stop() {
        audioPlayerManager?.stopAll()
    }

    open fun reachEnd(): Boolean = nextIndex >= soundKeyFrames.size
}