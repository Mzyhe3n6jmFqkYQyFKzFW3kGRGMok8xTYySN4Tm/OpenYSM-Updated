package com.elfmcys.yesstevemodel.audio

import net.minecraft.client.sounds.AudioStream
import org.lwjgl.BufferUtils
import java.io.IOException
import java.nio.ByteBuffer
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.UnsupportedAudioFileException

class AudioStreamWrapper(
    private val streamFactory: IAudioStreamFactory
) : IAudioStreamSupport {
    private val audioFormat: AudioFormat
    private var currentStream: AudioStream? = null

    @Volatile
    private var closed: Boolean = false

    init {
        try {
            val initialStream = streamFactory.openStream()
            currentStream = initialStream
            audioFormat = initialStream.format
        } catch (e: Exception) {
            when (e) {
                is IOException, is UnsupportedAudioFileException -> {
                    e.printStackTrace()
                    throw e
                }

                else -> throw e
            }
        }
    }

    override fun getFormat(): AudioFormat = audioFormat

    @Throws(IOException::class)
    override fun read(size: Int): ByteBuffer {
        val stream = currentStream
        if (stream != null) {
            var byteBuffer = stream.read(size)
            if (byteBuffer.remaining() == 0) {
                currentStream = null
                try {
                    reset()
                    val newStream = streamFactory.openStream()
                    currentStream = newStream
                    byteBuffer = newStream.read(size)
                    if (byteBuffer.remaining() == 0) {
                        reset()
                        return EMPTY_BUFFER
                    }
                } catch (th: Throwable) {
                    th.printStackTrace()
                    return EMPTY_BUFFER
                }
            }
            return byteBuffer
        }
        return EMPTY_BUFFER
    }

    @Throws(IOException::class)
    override fun close() {
        closed = true
        reset()
    }

    override fun isClosed(): Boolean = closed

    @Throws(IOException::class)
    fun reset() {
        currentStream?.let {
            it.close()
            currentStream = null
        }
    }

    companion object {
        private val EMPTY_BUFFER: ByteBuffer = BufferUtils.createByteBuffer(0)
    }
}
