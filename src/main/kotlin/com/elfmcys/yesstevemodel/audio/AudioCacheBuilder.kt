package com.elfmcys.yesstevemodel.audio

import io.netty.buffer.ByteBuf
import io.netty.buffer.PooledByteBufAllocator
import it.unimi.dsi.fastutil.ints.IntArrayList
import org.lwjgl.BufferUtils
import java.nio.ByteBuffer

class AudioCacheBuilder(
    private val cacheProvider: AudioStreamCache.CachedAudioStreamProvider,
    private val trackData: AudioTrackData
) {
    private val audioBuffer: ByteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(trackData.duration.toInt() * 2)
    private val chunkSizes: IntArrayList = IntArrayList(5)
    private var isClosed: Boolean = false

    fun appendAudio(byteBuffer: ByteBuffer) {
        if (!isClosed && audioBuffer.writableBytes() > 0) {
            val iMin = audioBuffer.writableBytes().coerceAtMost(byteBuffer.remaining())
            byteBuffer.limit(byteBuffer.position() + iMin)
            audioBuffer.writeBytes(byteBuffer)
            chunkSizes.add(iMin)
        }
    }

    fun flushToCache() {
        if (!isClosed) {
            isClosed = true
            val byteBuffer = BufferUtils.createByteBuffer(audioBuffer.readableBytes())
            audioBuffer.readBytes(byteBuffer.duplicate())
            audioBuffer.release()
            cacheProvider.cacheAudioData(trackData, byteBuffer, chunkSizes)
        }
    }
}
