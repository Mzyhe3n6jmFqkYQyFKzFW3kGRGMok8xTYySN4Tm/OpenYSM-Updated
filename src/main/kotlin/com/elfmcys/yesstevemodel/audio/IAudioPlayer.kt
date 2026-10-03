package com.elfmcys.yesstevemodel.audio

interface IAudioPlayer {
    fun release()
    fun isStopped(): Boolean
}
