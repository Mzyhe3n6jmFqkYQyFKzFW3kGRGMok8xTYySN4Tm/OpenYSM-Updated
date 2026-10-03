@file:Suppress("unused")

package com.elfmcys.yesstevemodel.audio

import java.nio.ByteBuffer

class AudioTrackData(
    byteBuffer: ByteBuffer?,
    codecType: Int,
    val sampleRate: Int,
    val duration: Long
) {
    val data: ByteBuffer? = byteBuffer?.let { buf ->
        val bufCopy = if (codecType == 2) {
            ByteBuffer.allocateDirect(buf.remaining())
        } else {
            ByteBuffer.allocate(buf.remaining())
        }
        bufCopy.duplicate().put(buf.duplicate())
        bufCopy
    }

    val codec: AudioCodec = when (codecType) {
        1 -> AudioCodec.VORBIS
        2 -> AudioCodec.OPUS
        else -> AudioCodec.UNDEFINED
    }
}
