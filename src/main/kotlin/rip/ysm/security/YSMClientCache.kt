package rip.ysm.security

import com.elfmcys.yesstevemodel.Constants
import rip.ysm.algorithms.CityHash
import rip.ysm.algorithms.MT19937
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.util.*

object YSMClientCache {
    @JvmStatic
    fun generateCacheFileName(hash1: Long, hash2: Long, rtKey: ByteArray?): String? {
        if (rtKey == null || rtKey.size != 56) return null
        val seed = 114514 // todo: 换成真随机数

        val mt = MT19937(Integer.toUnsignedLong(seed))
        val m1 = hash1 xor mt.extract_number()
        val m2 = hash2 xor mt.extract_number()

        val buf = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN)
        buf.putInt(seed)
        buf.putLong(m1)
        buf.putLong(m2)
        val bufArray = buf.array()

        for (i in bufArray.indices) {
            bufArray[i] = (bufArray[i].toInt() xor rtKey[i % rtKey.size].toInt()).toByte()
        }

        val sb = java.lang.StringBuilder(40)
        for (b in bufArray) {
            sb.append(String.format("%02x", b.toInt() and 0xFF))
        }
        return sb.toString()
    }

    @JvmStatic
    fun verifyFileContent(cacheFile: File?, hash1: Long, hash2: Long): Boolean {
        return !(cacheFile == null || !cacheFile.exists() || cacheFile.length() <= 8) && runCatching {
            val fileData = Files.readAllBytes(cacheFile.toPath())
            val payloadLen = fileData.size - 8

            val realHash = ByteBuffer.wrap(fileData, payloadLen, 8).order(ByteOrder.LITTLE_ENDIAN).long

            val payload = fileData.copyOfRange(0, payloadLen)
            val ch = CityHash()
            val calculatedHash = ch.hash64WithSeed(payload, YsmCrypt.SEED_CACHE_VERIFICATION)

            val verif = calculatedHash xor hash1 xor hash2
            verif == realHash
        }.getOrElse {
            Constants.LOGGER.error("Failed to verify cache file content", it)
            false
        }
    }

    @JvmStatic
    fun getModelUUIDFromFileName(fileName: String?, rtKey: ByteArray?): UUID? {
        if (fileName == null || fileName.length != 40 || rtKey == null || rtKey.size != 56) return null
        return runCatching {
            val buf = ByteArray(20)
            for (i in 0 until 20) {
                val high = Character.digit(fileName[i * 2], 16)
                val low = Character.digit(fileName[i * 2 + 1], 16)
                if (high == -1 || low == -1) return null
                buf[i] = ((high shl 4) or low).toByte()
            }

            for (i in buf.indices) {
                buf[i] = (buf[i].toInt() xor rtKey[i % rtKey.size].toInt()).toByte()
            }

            val byteBuf = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN)
            val seed = byteBuf.int
            val m1 = byteBuf.long
            val m2 = byteBuf.long

            val mt = MT19937(Integer.toUnsignedLong(seed))
            val hash1 = m1 xor mt.extract_number()
            val hash2 = m2 xor mt.extract_number()

            UUID(hash1, hash2)
        }.getOrNull()
    }

    @JvmStatic
    fun buildCacheIndex(cacheDir: File, rtKey: ByteArray): MutableMap<UUID, File> {
        val cacheIndex = HashMap<UUID, File>()

        if (!cacheDir.exists() || !cacheDir.isDirectory) return cacheIndex

        val files = cacheDir.listFiles() ?: return cacheIndex

        Constants.LOGGER.info("scanning cache directory")
        for (file in files) {
            if (file.isFile) {
                val realModelUuid = getModelUUIDFromFileName(file.name, rtKey)
                if (realModelUuid != null) {
                    cacheIndex[realModelUuid] = file
                }
            }
        }
        Constants.LOGGER.info("indexed {} cached models.", cacheIndex.size)
        return cacheIndex
    }
}