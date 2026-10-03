package rip.ysm.algorithms

import rip.ysm.security.YsmCrypt
import java.nio.charset.StandardCharsets
import java.util.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class YsmZstdTest {

    @Test
    fun testYsmZstdRoundTrip() {
        val originalText = "Hello OpenYSM! Testing YsmZstd compression and decompression logic.".repeat(100)
        val rawData = originalText.toByteArray(StandardCharsets.UTF_8)

        val compressed = YsmZstd.compress(rawData)
        val decompressed = YsmZstd.decompress(compressed)

        assertContentEquals(rawData, decompressed)
    }

    @Test
    fun testYsmZstdWithTrailingBytes() {
        val originalText = "Testing decompression with trailing checksum / padding bytes.".repeat(200)
        val rawData = originalText.toByteArray(StandardCharsets.UTF_8)

        val compressed = YsmZstd.compress(rawData)
        // Append 4 dummy checksum bytes and 16 padding bytes to simulate realistic frame payload
        val dataWithTrailing = ByteArray(compressed.size + 20)
        System.arraycopy(compressed, 0, dataWithTrailing, 0, compressed.size)
        val random = Random(42)
        val trailing = ByteArray(20)
        random.nextBytes(trailing)
        System.arraycopy(trailing, 0, dataWithTrailing, compressed.size, 20)

        val decompressed = YsmZstd.decompress(dataWithTrailing, 0, dataWithTrailing.size)
        assertContentEquals(rawData, decompressed)
    }

    @Test
    fun testVariousSizesWithTrailingBytes() {
        val sizes = intArrayOf(10, 64, 256, 1024, 8192, 65536, 262144, 1048576)
        val random = Random(999)

        for (size in sizes) {
            val rawData = ByteArray(size)
            random.nextBytes(rawData)

            val compressed = YsmZstd.compress(rawData)
            val trailingCount = random.nextInt(32) + 4
            val dataWithTrailing = ByteArray(compressed.size + trailingCount)
            System.arraycopy(compressed, 0, dataWithTrailing, 0, compressed.size)
            val trailing = ByteArray(trailingCount)
            random.nextBytes(trailing)
            System.arraycopy(trailing, 0, dataWithTrailing, compressed.size, trailingCount)

            val decompressed = YsmZstd.decompress(dataWithTrailing, 0, dataWithTrailing.size)
            assertContentEquals(rawData, decompressed, "Failed for size $size with $trailingCount trailing bytes")
        }
    }

    @Test
    fun testYsmCryptServerToClientCacheRoundTrip() {
        val payload = "Model JSON / Binary Payload data content for testing cache transcode and read.".repeat(500)
            .toByteArray(StandardCharsets.UTF_8)

        val serverKey = ByteArray(56)
        val clientKey = ByteArray(56)
        val random = Random(12345)
        random.nextBytes(serverKey)
        random.nextBytes(clientKey)

        val hashes = YsmCrypt.calculateModelHashes("test_sha256_hash", serverKey)
        val serverCache = YsmCrypt.encryptServerCache(payload, serverKey, hashes[0], hashes[1])

        val clientCache = YsmCrypt.transcodeServerDataToClientCache(
            serverCache,
            serverKey,
            clientKey,
            hashes[0],
            hashes[1]
        )

        val clearText = YsmCrypt.read(clientCache, clientKey)
        assertContentEquals(payload, clearText)
    }

    @Test
    fun testYsmCryptYsmFileRoundTrip() {
        val modelContent = "YSM Model Content with lots of data to compress and encrypt.".repeat(300)
            .toByteArray(StandardCharsets.UTF_8)

        val encryptedYsmFile = YsmCrypt.encryptYsmFile(modelContent)
        val decrypted = YsmCrypt.decryptYsmFile(encryptedYsmFile)

        assertContentEquals(modelContent, decrypted)
    }
}
