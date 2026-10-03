package com.elfmcys.yesstevemodel.audio

import com.elfmcys.yesstevemodel.Constants
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.Unpooled
import org.concentus.OpusDecoder
import org.gagravarr.ogg.OggFile
import org.gagravarr.opus.OpusFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer
import javax.sound.sampled.UnsupportedAudioFileException

class NativeAudioDecoder {
    private var inputSet: Boolean = false
    private var channels: Int = 0
    private var oggFile: OggFile? = null
    private var opusFile: OpusFile? = null
    private var opusDecoder: OpusDecoder? = null
    private var opusPcmBuffer: ByteBuffer? = null
    private var opusPcmShortBuffer: ShortBuffer? = null
    private var decodePcmArray: ShortArray? = null

    fun openStream(input: ByteBuffer): Boolean {
        require(input.isDirect) { "input is not direct buffer" }

        return try {
            val ogg = OggFile(ByteBufInputStream(Unpooled.wrappedBuffer(input.slice())))
            oggFile = ogg

            val opus = try {
                OpusFile(ogg)
            } catch (_: IllegalArgumentException) {
                throw UnsupportedAudioFileException("File is not a valid Opus audio stream.")
            }
            opusFile = opus

            val numChannels = opus.info.numChannels
            if (numChannels != 1 && numChannels != 2) {
                throw UnsupportedAudioFileException("Unsupported Opus channels: $numChannels")
            }
            channels = numChannels

            opusDecoder = OpusDecoder(48000, numChannels)
            val pcmArray = ShortArray(5760 * numChannels)
            decodePcmArray = pcmArray

            var pcmBuf = opusPcmBuffer
            if (pcmBuf == null || pcmBuf.capacity() < pcmArray.size * 2) {
                val newBuf = ByteBuffer.allocateDirect(pcmArray.size * 2).order(ByteOrder.nativeOrder())
                opusPcmBuffer = newBuf
                opusPcmShortBuffer = newBuf.asShortBuffer()
                pcmBuf = newBuf
            }
            pcmBuf.position(0)
            pcmBuf.limit(0)

            inputSet = true
            true
        } catch (e: Exception) {
            destroyEngines()
            e.printStackTrace()
            false
        }
    }

    fun decodeFrame(output: ByteBuffer): Int {
        require(output.isDirect) { "output is not direct buffer" }
        val decoder = opusDecoder
        val file = opusFile
        val pcmBuf = opusPcmBuffer
        val pcmShortBuf = opusPcmShortBuffer
        val pcmArray = decodePcmArray

        if (!inputSet || decoder == null || file == null || pcmBuf == null || pcmShortBuf == null || pcmArray == null) {
            return -1
        }

        return try {
            output.order(ByteOrder.LITTLE_ENDIAN)
            val requestedMonoSamples = output.remaining() / 2
            if (requestedMonoSamples <= 0) return 0

            var monoSamplesGenerated = 0

            while (output.remaining() >= 2) {
                if (pcmBuf.hasRemaining()) {
                    if (channels == 2) {
                        while (pcmBuf.remaining() >= 4 && output.remaining() >= 2) {
                            val left = pcmBuf.short
                            val right = pcmBuf.short
                            val mixed = ((left + right) / 2).toShort()
                            output.putShort(mixed)
                            monoSamplesGenerated++
                        }
                    } else {
                        while (pcmBuf.hasRemaining() && output.remaining() >= 2) {
                            output.putShort(pcmBuf.short)
                            monoSamplesGenerated++
                        }
                    }
                    if (output.remaining() < 2) break
                }

                val audioData = file.nextAudioPacket ?: break // EOF

                val data = audioData.data
                if (data == null || data.isEmpty()) continue

                runCatching {
                    val decodedSamplesPerChannel = decoder.decode(data, 0, data.size, pcmArray, 0, 5760, false)
                    if (decodedSamplesPerChannel > 0) {
                        val totalShorts = decodedSamplesPerChannel * channels
                        pcmBuf.clear()
                        pcmShortBuf.clear()
                        pcmShortBuf.put(pcmArray, 0, totalShorts)
                        pcmBuf.position(0)
                        pcmBuf.limit(totalShorts * 2)
                    }
                }.onFailure {
                    Constants.LOGGER.error("Opus decode error on valid payload: ${it.localizedMessage}", it)
                }
            }

            monoSamplesGenerated * 2
        } catch (e: Exception) {
            e.printStackTrace()
            -100
        }
    }

    fun reset() {
        inputSet = false
        destroyEngines()
    }

    fun destroy() {
        inputSet = false
        destroyEngines()
    }

    private fun destroyEngines() {
        try {
            opusFile?.close()
            oggFile?.close()
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
        opusDecoder = null
        opusFile = null
        oggFile = null

        opusPcmBuffer?.clear()
    }
}
