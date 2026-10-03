package com.elfmcys.yesstevemodel.audio

import java.io.IOException
import javax.sound.sampled.UnsupportedAudioFileException

fun interface IAudioStreamFactory {
    @Throws(UnsupportedAudioFileException::class, IOException::class)
    fun openStream(): IAudioStreamSupport
}
