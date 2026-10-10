@file:Suppress("unused")

package rip.ysm.legacy

import java.io.ByteArrayOutputStream
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.Inflater

object DeflateUtil {
    fun compressBytes(input: ByteArray): ByteArray {
        if (input.isEmpty()) return byteArrayOf()
        val stream = ByteArrayOutputStream()
        val buf = ByteArray(1024)
        val deflater = Deflater(Deflater.BEST_COMPRESSION)
        deflater.setInput(input)
        deflater.finish()
        while (!deflater.finished()) {
            val compressedDataLength = deflater.deflate(buf)
            stream.write(buf, 0, compressedDataLength)
        }
        deflater.end()
        return stream.toByteArray()
    }

    @Throws(DataFormatException::class)
    fun decompressBytes(input: ByteArray): ByteArray {
        if (input.isEmpty()) return byteArrayOf()
        val stream = ByteArrayOutputStream()
        val buf = ByteArray(1024)
        val inflater = Inflater()
        inflater.setInput(input, 0, input.size)
        while (!inflater.finished()) {
            val resultLength = inflater.inflate(buf)
            stream.write(buf, 0, resultLength)
        }
        inflater.end()
        return stream.toByteArray()
    }
}