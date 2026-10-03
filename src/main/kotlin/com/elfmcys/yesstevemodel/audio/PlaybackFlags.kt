package com.elfmcys.yesstevemodel.audio

class PlaybackFlags(private val isAudioEnabled: Boolean) {
    private var paused: Boolean = false
    private var stopped: Boolean = false
    private var audioPlayerManager: AudioPlayerManager? = null

    fun setPaused(paused: Boolean) {
        this.paused = paused
    }

    fun setStopped(stopped: Boolean) {
        this.stopped = stopped
    }

    fun isPaused(): Boolean = paused

    fun isStopped(): Boolean = stopped

    fun getAudioPlayerManager(): AudioPlayerManager? {
        if (!isAudioEnabled) {
            return null
        }
        if (audioPlayerManager == null) {
            audioPlayerManager = AudioPlayerManager()
        }
        return audioPlayerManager
    }
}
