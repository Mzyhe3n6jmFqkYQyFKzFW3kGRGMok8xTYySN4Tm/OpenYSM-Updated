package com.elfmcys.yesstevemodel.audio

import it.unimi.dsi.fastutil.ints.IntArrayList
import org.lwjgl.BufferUtils
import java.io.IOException
import java.nio.ByteBuffer
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.UnsupportedAudioFileException

class SeekableAudioStream(
    private val audioData: ByteBuffer,
    private val seekPoints: IntArrayList,
    private val audioFormat: AudioFormat
) : IAudioStreamSupport {
    init {
        if (audioFormat.channels != 1) {
            throw UnsupportedAudioFileException()
        }
    }

    private var position: Int = 0
    private var readLimit: Int = 0

    @Volatile
    private var closed: Boolean = false

    override fun getFormat(): AudioFormat = audioFormat

    @Throws(IOException::class)
    override fun read(size: Int): ByteBuffer {
        if (readLimit >= seekPoints.size || closed) {
            return EMPTY_BUFFER
        }
        val chunkSize = seekPoints.getInt(readLimit)
        val slice = audioData.slice(position, chunkSize)
        readLimit++
        position += chunkSize
        return slice
    }

    @Throws(IOException::class)
    override fun close() {
        if (!closed) closed = true
    }

    override fun isClosed(): Boolean = closed

    companion object {
        private val EMPTY_BUFFER: ByteBuffer = BufferUtils.createByteBuffer(0)
    }
}
