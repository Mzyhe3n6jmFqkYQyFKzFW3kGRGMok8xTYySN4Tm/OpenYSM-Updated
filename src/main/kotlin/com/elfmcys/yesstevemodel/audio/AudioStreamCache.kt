package com.elfmcys.yesstevemodel.audio

import com.elfmcys.yesstevemodel.ResourceCleanupHelper
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.mojang.blaze3d.systems.RenderSystem
import it.unimi.dsi.fastutil.ints.IntArrayList
import net.minecraft.client.Minecraft
import java.io.IOException
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.UnsupportedAudioFileException

object AudioStreamCache {
    private val providerCache = IdentityHashMap<ModelAssembly, WeakReference<CachedAudioStreamProvider>>()
    private val LOCK = Any()

    @JvmStatic
    fun getOrCreateProvider(renderContext: ModelAssembly): IAudioStreamProvider {
        RenderSystem.assertOnRenderThread()
        val weakReference = providerCache[renderContext]
        if (weakReference != null) {
            val existingProvider = weakReference.get()
            if (existingProvider != null) return existingProvider
        }
        val newProvider = CachedAudioStreamProvider()
        ResourceCleanupHelper.registerCleanup(newProvider, renderContext) {
            Minecraft.getInstance().execute {
                providerCache.remove(it)
            }
        }
        providerCache[renderContext] = WeakReference(newProvider)
        return newProvider
    }

    class CachedAudioStreamProvider : IAudioStreamProvider {
        private val cachedEntries = ConcurrentHashMap<AudioTrackData, CachedAudioEntry>()
        private val pendingTracks = ConcurrentHashMap<AudioTrackData, Any>()

        fun cacheAudioData(trackData: AudioTrackData, byteBuffer: ByteBuffer, intArrayList: IntArrayList) {
            cachedEntries[trackData] = CachedAudioEntry(
                byteBuffer,
                AudioFormat(trackData.sampleRate.toFloat(), 16, 1, true, false),
                intArrayList
            )
            pendingTracks.remove(trackData)
        }

        @Throws(UnsupportedAudioFileException::class, IOException::class)
        override fun createAudioStream(trackData: AudioTrackData): IAudioStreamSupport {
            val audioEntry = cachedEntries[trackData]
            if (audioEntry != null) {
                return SeekableAudioStream(
                    audioEntry.audioData.duplicate(),
                    audioEntry.seekPositions,
                    audioEntry.audioFormat
                )
            }
            val cacheBuilder: AudioCacheBuilder?
            if (trackData.duration / trackData.sampleRate <= 4 && !pendingTracks.containsKey(trackData)) {
                cacheBuilder = AudioCacheBuilder(this, trackData)
                pendingTracks[trackData] = LOCK
            } else {
                cacheBuilder = null
            }
            val data = trackData.data ?: throw UnsupportedAudioFileException()
            return when (trackData.codec) {
                AudioCodec.VORBIS -> OggVorbisAudioStream(data, cacheBuilder)
                AudioCodec.OPUS -> OggOpusAudioStream(data, cacheBuilder)
                else -> throw UnsupportedAudioFileException()
            }
        }

        @JvmRecord
        private data class CachedAudioEntry(
            val audioData: ByteBuffer,
            val audioFormat: AudioFormat,
            val seekPositions: IntArrayList
        )
    }
}
