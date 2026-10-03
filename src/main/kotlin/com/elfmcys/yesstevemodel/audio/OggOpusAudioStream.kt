package com.elfmcys.yesstevemodel.audio

import com.elfmcys.yesstevemodel.Constants
import io.netty.buffer.ByteBuf
import io.netty.buffer.PooledByteBufAllocator
import org.lwjgl.BufferUtils
import java.io.IOException
import java.nio.ByteBuffer
import javax.sound.sampled.AudioFormat

class OggOpusAudioStream(
    byteBuffer: ByteBuffer,
    private val cacheBuilder: AudioCacheBuilder?
) : IAudioStreamSupport {
    @Volatile
    private var closed: Boolean = false
    private var endOfStream: Boolean = false

    private val decoder: NativeAudioDecoder = ObjectPool.acquire()
    private val outputBuffer: ByteBuf = PooledByteBufAllocator.DEFAULT.buffer(AUDIO_FORMAT.sampleRate.toInt() * 2)

    init {
        outputBuffer.retain()
        decoder.openStream(byteBuffer)
    }

    override fun getFormat(): AudioFormat = AUDIO_FORMAT

    @Throws(IOException::class)
    override fun read(i: Int): ByteBuffer {
        if (i == 0 || endOfStream || closed) {
            return EMPTY_BUFFER
        }
        if (outputBuffer.capacity() < i) {
            outputBuffer.capacity(i)
        }
        val byteBufferNioBuffer = outputBuffer.nioBuffer(0, i)
        val decodedBytes = decoder.decodeFrame(byteBufferNioBuffer.duplicate())
        if (decodedBytes <= 0) {
            if (decodedBytes == 0 && cacheBuilder != null) {
                cacheBuilder.flushToCache()
            }
            if (decodedBytes < 0) {
                Constants.LOGGER.error("Decoder error: {}", decodedBytes)
            }
            endOfStream = true
            return EMPTY_BUFFER
        }
        val byteBufferSlice = byteBufferNioBuffer.slice(0, decodedBytes)
        cacheBuilder?.appendAudio(byteBufferSlice.slice())
        return byteBufferSlice
    }

    @Throws(IOException::class)
    override fun close() {
        if (!closed) {
            outputBuffer.release()
            ObjectPool.release(decoder)
            closed = true
        }
    }

    override fun isClosed(): Boolean = closed

    private companion object {
        private val EMPTY_BUFFER: ByteBuffer = BufferUtils.createByteBuffer(0)
        private val AUDIO_FORMAT: AudioFormat = AudioFormat(48000.0f, 16, 1, true, false)
    }
}
