@file:Suppress("MemberVisibilityCanBePrivate")

package com.elfmcys.yesstevemodel.audio

class PlaybackFlags(private val isAudioEnabled: Boolean) {
    private var isPaused: Boolean = false
    private var isStopped: Boolean = false
    private var audioPlayerManager2: AudioPlayerManager? = null

    val audioPlayerManager: AudioPlayerManager?
        get() {
            if (!isAudioEnabled) return null
            if (audioPlayerManager2 == null) audioPlayerManager2 = AudioPlayerManager()
            return audioPlayerManager2
        }

    fun isPaused(): Boolean = isPaused
    fun setPaused(paused: Boolean) { this.isPaused = paused }
    fun isStopped(): Boolean = isStopped
    fun setStopped(stopped: Boolean) { this.isStopped = stopped }
}
