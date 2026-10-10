package rip.ysm.security

import com.elfmcys.yesstevemodel.Constants
import java.io.File
import java.nio.file.Files
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class YSMClientCacheTest {
    @Test
    fun testGenerateCacheFileNameRandomizedAndDecodable() {
        val rtKey = ByteArray(56)
        val rand = Random(42)
        rand.nextBytes(rtKey)

        val hash1 = 0x123456789ABCDEFL
        val hash2 = -0x1edcba9876543210L
        val expectedUuid = UUID(hash1, hash2)

        val fileNames = mutableSetOf<String>()
        val count = 50

        for (i in 0 until count) {
            val fileName = YSMClientCache.generateCacheFileName(hash1, hash2, rtKey)
            assertNotNull(fileName)
            assertEquals(40, fileName.length)
            fileNames.add(fileName)

            val decodedUuid = YSMClientCache.getModelUUIDFromFileName(fileName, rtKey)
            assertEquals(expectedUuid, decodedUuid)
        }

        assertEquals(count, fileNames.size, "Generated file names should be unique due to random seed")
    }

    @Test
    fun testInvalidInputHandling() {
        val validKey = ByteArray(56)
        val hash1 = 100L
        val hash2 = 200L

        assertNull(YSMClientCache.generateCacheFileName(hash1, hash2, null))
        assertNull(YSMClientCache.generateCacheFileName(hash1, hash2, ByteArray(32)))

        assertNull(YSMClientCache.getModelUUIDFromFileName(null, validKey))
        assertNull(YSMClientCache.getModelUUIDFromFileName("short", validKey))
        assertNull(YSMClientCache.getModelUUIDFromFileName("0123456789abcdef0123456789abcdef01234567", null))
        assertNull(YSMClientCache.getModelUUIDFromFileName("0123456789abcdef0123456789abcdef01234567", ByteArray(32)))
        assertNull(YSMClientCache.getModelUUIDFromFileName("zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz", validKey))
    }

    @Test
    fun testVerifyCacheContentAndFormat() {
        val serverKey = ByteArray(56)
        val clientKey = ByteArray(56)
        val random = Random(123)
        random.nextBytes(serverKey)
        random.nextBytes(clientKey)

        val hashes = YsmCrypt.calculateModelHashes("model_hash_test", serverKey)
        val payload = "dummy_payload_bytes".toByteArray()

        val serverCache = YsmCrypt.encryptServerCache(payload, serverKey, hashes[0], hashes[1])
        assertTrue(YsmCrypt.verifyServerCache(serverCache, hashes[0], hashes[1]))
        assertFalse(YsmCrypt.verifyServerCache(serverCache, hashes[0] + 1, hashes[1]))

        val clientCache = YsmCrypt.transcodeServerDataToClientCache(
            serverCache,
            serverKey,
            clientKey,
            hashes[0],
            hashes[1]
        )

        val tempFile = File.createTempFile("ysm_cache_", ".dat")
        tempFile.deleteOnExit()
        Files.write(tempFile.toPath(), clientCache)

        assertTrue(YSMClientCache.verifyFileContent(tempFile, hashes[0], hashes[1]))
        assertFalse(YSMClientCache.verifyFileContent(tempFile, hashes[0] + 1, hashes[1]))
        assertFalse(YSMClientCache.verifyFileContent(null, hashes[0], hashes[1]))
    }
}
