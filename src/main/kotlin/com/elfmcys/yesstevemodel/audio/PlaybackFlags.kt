@file:Suppress("MemberVisibilityCanBePrivate")

package com.elfmcys.yesstevemodel.audio

class PlaybackFlags(private val isAudioEnabled: Boolean) {
    var isPaused: Boolean = false
        private set
    var isStopped: Boolean = false
        private set

    var audioPlayerManager: AudioPlayerManager? = null
        get() {
            if (!isAudioEnabled) return null
            if (field == null) field = AudioPlayerManager()
            return field
        }

    fun setPaused(paused: Boolean) {
        this.isPaused = paused
    }

    fun setStopped(stopped: Boolean) {
        this.isStopped = stopped
    }
}
