package com.elfmcys.yesstevemodel.audio

import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.Unpooled
import it.unimi.dsi.fastutil.floats.FloatArrayList
import net.minecraft.client.sounds.JOrbisAudioStream
import org.lwjgl.BufferUtils
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.UnsupportedAudioFileException
import kotlin.math.roundToInt

class OggVorbisAudioStream(
    byteBuffer: ByteBuffer,
    private val cacheBuilder: AudioCacheBuilder?
) : IAudioStreamSupport {
    private val oggStream: JOrbisAudioStream = JOrbisAudioStream(ByteBufInputStream(Unpooled.wrappedBuffer(byteBuffer)))
    private val audioFormat: AudioFormat
    private val channels: Int
    private val pendingSamples: FloatArrayList = FloatArrayList()

    @Volatile
    private var isClosed: Boolean = false
    private var isEndOfStream: Boolean = false

    init {
        val sourceFormat = oggStream.format
        channels = sourceFormat.channels
        if (channels != 1 && channels != 2) throw UnsupportedAudioFileException()
        audioFormat = AudioFormat(sourceFormat.sampleRate, 16, 1, true, false)
    }

    override fun getFormat(): AudioFormat = audioFormat

    @Throws(IOException::class)
    override fun read(requestedBytes: Int): ByteBuffer {
        if (isEndOfStream || isClosed) {
            return EMPTY_BUFFER
        }
        val requestedMonoSamples = requestedBytes / 2
        val neededFloatSamples = requestedMonoSamples * channels
        while (pendingSamples.size < neededFloatSamples) {
            val more = oggStream.readChunk(pendingSamples::add)
            if (!more) {
                isEndOfStream = true
                break
            }
        }
        val availableFloats = pendingSamples.size.coerceAtMost(neededFloatSamples)
        val outMonoSamples = availableFloats / channels
        val outBytes = outMonoSamples * 2
        if (outBytes <= 0) {
            if (cacheBuilder != null && isEndOfStream) {
                cacheBuilder.flushToCache()
            }
            return EMPTY_BUFFER
        }
        val out = BufferUtils.createByteBuffer(outBytes).order(ByteOrder.nativeOrder())
        if (channels == 1) {
            for (i in 0 until outMonoSamples) {
                val v = pendingSamples.getFloat(i)
                out.putShort(floatToPcm16(v))
            }
        } else {
            for (i in 0 until outMonoSamples) {
                val l = pendingSamples.getFloat(i * 2)
                val r = pendingSamples.getFloat((i * 2) + 1)
                out.putShort(floatToPcm16((l + r) * 0.5f))
            }
        }
        pendingSamples.removeElements(0, outMonoSamples * channels)
        out.flip()
        if (cacheBuilder != null) {
            cacheBuilder.appendAudio(out.duplicate())
            if (isEndOfStream) {
                cacheBuilder.flushToCache()
            }
        }
        return out
    }

    @Throws(IOException::class)
    override fun close() {
        if (!isClosed) {
            oggStream.close()
            isClosed = true
        }
    }

    override fun isClosed(): Boolean = isClosed

    companion object {
        private val EMPTY_BUFFER: ByteBuffer = BufferUtils.createByteBuffer(0)

        private fun floatToPcm16(v: Float): Short {
            val clamped = when {
                v > 1.0f -> 1.0f
                v < -1.0f -> -1.0f
                else -> v
            }
            return (clamped * 32767.0f).roundToInt().toShort()
        }
    }
}
