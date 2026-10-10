package rip.ysm.algorithms

import io.airlift.compress.zstd.ZstdCompressor
import io.airlift.compress.zstd.ZstdDecompressor
import io.airlift.compress.zstd.ZstdInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

object YsmZstd {
    @JvmStatic
    @Throws(IOException::class)
    fun decompress(rawData: ByteArray): ByteArray {
        return decompress(rawData, 0, rawData.size)
    }

    @JvmStatic
    @Throws(IOException::class)
    fun decompress(rawData: ByteArray, offset: Int, length: Int): ByteArray {
        val actualLength = washInPlace(rawData, offset, length)
        val uncompressedSize = ZstdDecompressor.getDecompressedSize(rawData, offset, actualLength)
        if (uncompressedSize >= 0) {
            val output = ByteArray(uncompressedSize.toInt())
            val decompressor = ZstdDecompressor()
            decompressor.decompress(rawData, offset, actualLength, output, 0, output.size)
            return output
        }
        ZstdInputStream(ByteArrayInputStream(rawData, offset, actualLength)).use { input ->
            val out = ByteArrayOutputStream(maxOf(64 * 1024, actualLength * 2))
            val buffer = ByteArray(64 * 1024)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
            return out.toByteArray()
        }
    }

    @JvmStatic
    fun compress(rawData: ByteArray): ByteArray {
        return compress(rawData, 0, rawData.size)
    }

    @JvmStatic
    fun compress(rawData: ByteArray, offset: Int, length: Int): ByteArray {
        val compressor = ZstdCompressor()
        val buffer = ByteArray(compressor.maxCompressedLength(length))
        val compressedLength = compressor.compress(rawData, offset, length, buffer, 0, buffer.size)
        val zstdData = buffer.copyOf(compressedLength)
        return obfuscate(zstdData)
    }

    private fun wash(data: ByteArray): ByteArray {
        washInPlace(data, 0, data.size)
        return data
    }

    private fun washInPlace(data: ByteArray?, base: Int, length: Int): Int {
        if (data == null || length < 5) {
            throw IllegalArgumentException("Invalid data length")
        }

        val magic = data[base].toInt() and 0xFF or
                (data[base + 1].toInt() and 0xFF shl 8) or
                (data[base + 2].toInt() and 0xFF shl 16) or
                (data[base + 3].toInt() and 0xFF shl 24)
        if (magic != 0xFD2FB528.toInt()) {
            throw IllegalArgumentException("Not a standard ZSTD Magic Number. May be skippable frame or unknown.")
        }

        val fhd = data[base + 4]
        data[base + 4] = (fhd.toInt() and 0xFB).toByte()

        val frameHeaderSize = calculateFrameHeaderSize(fhd)
        var offset = base + 4 + frameHeaderSize
        val end = base + length

        var frameEnd = end
        while (offset + 3 <= end) {
            val b0 = data[offset].toInt() and 0xFF
            val b1 = data[offset + 1].toInt() and 0xFF
            val b2 = data[offset + 2].toInt() and 0xFF
            val lastBlock = b0 shr 7 and 1
            val blockTypeYSM = b0 shr 5 and 3

            val rawSize = b0 and 0x1F shl 16 or b1 or (b2 shl 8)
            val cSize = rawSize xor 0xD4E9
            val blockTypeStd = when (blockTypeYSM) {
                0 -> 2
                1 -> 1
                2 -> 3
                3 -> 0
                else -> throw IllegalStateException("Unknown block type")
            }

            val stdHeader = lastBlock or (blockTypeStd shl 1) or (cSize shl 3)

            data[offset] = (stdHeader and 0xFF).toByte()
            data[offset + 1] = (stdHeader shr 8 and 0xFF).toByte()
            data[offset + 2] = (stdHeader shr 16 and 0xFF).toByte()

            val blockDataSize = if (blockTypeStd == 1) 1 else cSize
            offset += 3 + blockDataSize

            if (lastBlock == 1) {
                frameEnd = offset
                break
            }
        }
        return frameEnd - base
    }

    private fun obfuscate(data: ByteArray?): ByteArray {
        if (data == null || data.size < 5) {
            throw IllegalArgumentException("Invalid data length")
        }

        val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
        val magic = buffer.getInt(0)
        if (magic != 0xFD2FB528.toInt()) {
            throw IllegalArgumentException("Not a standard ZSTD frame.")
        }

        val fhd = data[4]
        val frameHeaderSize = calculateFrameHeaderSize(fhd)
        var offset = 4 + frameHeaderSize

        while (offset + 3 <= data.size) {
            val b0 = data[offset].toInt() and 0xFF
            val b1 = data[offset + 1].toInt() and 0xFF
            val b2 = data[offset + 2].toInt() and 0xFF
            val cBlockHeader = b0 or (b1 shl 8) or (b2 shl 16)

            val lastBlock = cBlockHeader and 1
            val blockTypeStd = cBlockHeader shr 1 and 3
            val cSize = cBlockHeader shr 3

            val blockDataSize = if (blockTypeStd == 1) 1 else cSize

            val blockTypeYSM = when (blockTypeStd) {
                0 -> 3
                1 -> 1
                2 -> 0
                3 -> 2
                else -> throw IllegalStateException("Unknown block type")
            }

            val rawSize = cSize xor 0xD4E9
            val ysmB0 = lastBlock shl 7 or (blockTypeYSM shl 5) or (rawSize shr 16 and 0x1F)
            val ysmB1 = rawSize and 0xFF
            val ysmB2 = rawSize shr 8 and 0xFF

            data[offset] = ysmB0.toByte()
            data[offset + 1] = ysmB1.toByte()
            data[offset + 2] = ysmB2.toByte()

            offset += 3 + blockDataSize

            if (lastBlock == 1) {
                break
            }
        }

        return data
    }

    private fun calculateFrameHeaderSize(fhd: Byte): Int {
        val fhdInt = fhd.toInt() and 0xFF
        val singleSegment = fhdInt shr 5 and 1 == 1

        var dictIdSize = 0
        val dictIdBits = fhdInt and 3
        when (dictIdBits) {
            1 -> dictIdSize = 1
            2 -> dictIdSize = 2
            3 -> dictIdSize = 4
        }

        var fcsSize = 0
        val fcsBits = fhdInt shr 6 and 3
        when (fcsBits) {
            0 -> fcsSize = if (singleSegment) 1 else 0
            1 -> fcsSize = 2
            2 -> fcsSize = 4
            3 -> fcsSize = 8
        }

        val windowDescSize = if (singleSegment) 0 else 1

        return 1 + windowDescSize + dictIdSize + fcsSize
    }
}