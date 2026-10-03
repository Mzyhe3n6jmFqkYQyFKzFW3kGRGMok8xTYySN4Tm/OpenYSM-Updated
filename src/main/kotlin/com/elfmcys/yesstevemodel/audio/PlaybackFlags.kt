@file:Suppress("MemberVisibilityCanBePrivate")

package com.elfmcys.yesstevemodel.audio

class PlaybackFlags(private val isAudioEnabled: Boolean) {
    var paused: Boolean = false
    var stopped: Boolean = false
    private var audioPlayerManager2: AudioPlayerManager? = null

    val audioPlayerManager: AudioPlayerManager?
        get() {
            if (!isAudioEnabled) return null
            if (audioPlayerManager2 == null) audioPlayerManager2 = AudioPlayerManager()
            return audioPlayerManager2
        }

    fun setPaused(paused: Boolean) {
        this.paused = paused
    }

    fun setStopped(stopped: Boolean) {
        this.stopped = stopped
    }

    fun isPaused(): Boolean = paused

    fun isStopped(): Boolean = stopped
}
