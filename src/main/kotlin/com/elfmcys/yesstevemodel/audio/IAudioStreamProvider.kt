package com.elfmcys.yesstevemodel.audio

import java.io.IOException
import javax.sound.sampled.UnsupportedAudioFileException

fun interface IAudioStreamProvider {
    @Throws(UnsupportedAudioFileException::class, IOException::class)
    fun createAudioStream(trackData: AudioTrackData): IAudioStreamSupport
}
