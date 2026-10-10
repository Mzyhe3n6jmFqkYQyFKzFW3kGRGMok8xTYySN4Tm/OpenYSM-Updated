@file:Suppress("unused")

package rip.ysm.legacy

import com.elfmcys.yesstevemodel.util.DigestUtil
import it.unimi.dsi.fastutil.Pair
import it.unimi.dsi.fastutil.bytes.ByteArrays
import net.minecraft.resources.Identifier
import org.apache.commons.io.FileUtils
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.util.*
import java.util.zip.DataFormatException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object YesModelUtils {
    const val HEAD: Int = 0x59_53_47_50
    const val VERSION: Int = 0x00_00_00_01
    const val VERSION_II: Int = 0x00_00_00_02
    private const val ENCRYPTION_METHOD: String = "AES"

    fun getYsmCryptoVersion(fileData: ByteArray?): Int {
        if (fileData == null || fileData.size < 8) return -1

        // EF BB BF YSGP
        if (fileData[0] == 0xEF.toByte() && fileData[1] == 0xBB.toByte() && fileData[2] == 0xBF.toByte() &&
            fileData[3] == 0x59.toByte() && fileData[4] == 0x53.toByte() && fileData[5] == 0x47.toByte() && fileData[6] == 0x50.toByte()
        ) return 3

        if (fileData[0] == 0x59.toByte() && fileData[1] == 0x53.toByte() && fileData[2] == 0x47.toByte() && fileData[3] == 0x50.toByte()) {
            val cryptoVersion = ByteBuffer.wrap(fileData, 4, 4).order(ByteOrder.BIG_ENDIAN).getInt()
            when (cryptoVersion) {
                2 -> {
                    return 2
                }

                1 -> {
                    return 1
                }
            }
        }

        return -1
    }

    @Throws(IOException::class)
    fun input(data: ByteArray): Map<String, ByteArray> {
        if (data.size < 24) return emptyMap()
        val head = ByteInteger.bytes2Int(data, 0)
        val version = ByteInteger.bytes2Int(data, 4)
        return inputInternal(data, head, version)
    }

    @Throws(IOException::class)
    fun input(ysmFile: File): Map<String, ByteArray> {
        val fileName = removeExtension(ysmFile.name)
        if (Identifier.tryParse(fileName) == null) return emptyMap()
        val data = FileUtils.readFileToByteArray(ysmFile)
        val head = ByteInteger.bytes2Int(data, 0)
        val version = ByteInteger.bytes2Int(data, 4)
        return inputInternal(data, head, version)
    }

    @Throws(IOException::class)
    private fun inputInternal(data: ByteArray, head: Int, version: Int): Map<String, ByteArray> {
        if (head != HEAD) return emptyMap()
        if (version != VERSION && version != VERSION_II) return emptyMap()

        val md5 = ByteArrays.copy(data, 8, 16)
        val modelFilesData = ByteArrays.copy(data, 24, data.size - 24)
        if (!md5.contentEquals(DigestUtil.md5(modelFilesData))) return emptyMap()

        val outputs = HashMap<String, ByteArray>()
        val tmp = ByteArrayInputStream(modelFilesData)
        while (tmp.available() > 0) {
            runCatching {
                val ysmFileData: Pair<String, ByteArray> = if (version == VERSION) {
                    ysmToFile(tmp)
                } else {
                    ysmToFileNew(tmp)
                }
                outputs[ysmFileData.left()] = ysmFileData.right()
            }.onFailure { e ->
                e.printStackTrace()
            }
        }
        return outputs
    }

    @Throws(IOException::class, GeneralSecurityException::class, DataFormatException::class)
    private fun ysmToFile(tmp: ByteArrayInputStream): Pair<String, ByteArray> {
        val name = readString(tmp)
        val size = readInt(tmp)

        val passwordBytes = ByteArray(16)
        val ivBytes = ByteArray(16)
        tmp.read(passwordBytes)
        tmp.read(ivBytes)
        val key = SecretKeySpec(passwordBytes, ENCRYPTION_METHOD)
        val iv = IvParameterSpec(ivBytes)

        val fileData = ByteArray(size)
        tmp.read(fileData)

        val decryptData = AESUtil.decrypt(key, iv, fileData)
        val rawData = DeflateUtil.decompressBytes(decryptData.toByteArray())

        return Pair.of(name, rawData)
    }

    @Throws(IOException::class, GeneralSecurityException::class, DataFormatException::class)
    private fun ysmToFileNew(tmp: ByteArrayInputStream): Pair<String, ByteArray> {
        val fileName = readBase64String(tmp)
        val fileSize = readInt(tmp)
        val cipherSecretKeySize = readInt(tmp)

        val cipherSecretKey = ByteArray(cipherSecretKeySize)
        val ivBytes = ByteArray(16)
        val fileData = ByteArray(fileSize)
        tmp.read(cipherSecretKey)
        tmp.read(ivBytes)
        tmp.read(fileData)

        val keyFromMd5 = getKeyFromMd5(fileData)
        val secretSecretKey = AESUtil.getKey(keyFromMd5)
        val iv = IvParameterSpec(ivBytes)
        val decryptSecretKey = AESUtil.decrypt(secretSecretKey, iv, cipherSecretKey).toByteArray()
        val key = AESUtil.getKey(decryptSecretKey)
        val decryptData = AESUtil.decrypt(key, iv, fileData)
        val rawData = DeflateUtil.decompressBytes(decryptData.toByteArray())

        return Pair.of(fileName, rawData)
    }

    private fun getKeyFromMd5(fileData: ByteArray): ByteArray {
        val md5 = DigestUtil.md5(fileData)
        val random = Random(toLong(md5))
        val keys = ByteArray(16)
        random.nextBytes(keys)
        return keys
    }

    private fun toLong(bytes: ByteArray): Long {
        var value = 0L
        for (b in bytes) {
            value = (value shl 8) + (b.toLong() and 0xffL)
        }
        return value
    }

    @Throws(IOException::class)
    private fun readString(stream: ByteArrayInputStream): String {
        val size = readInt(stream)
        val stringBytes = ByteArray(size)
        stream.read(stringBytes)
        return String(stringBytes, StandardCharsets.UTF_8)
    }

    @Throws(IOException::class)
    private fun readBase64String(stream: ByteArrayInputStream): String {
        val size = readInt(stream)
        val stringBytes = ByteArray(size)
        stream.read(stringBytes)
        return String(Base64.getDecoder().decode(stringBytes), StandardCharsets.UTF_8)
    }

    @Throws(IOException::class)
    private fun readBoolean(stream: ByteArrayInputStream): Boolean {
        return readInt(stream) != 0
    }

    @Throws(IOException::class)
    private fun readInt(stream: ByteArrayInputStream): Int {
        val sizeBytes = ByteArray(4)
        stream.read(sizeBytes)
        return ByteInteger.bytes2Int(sizeBytes, 0)
    }

    private fun removeExtension(fileName: String): String {
        val lastIndex = fileName.lastIndexOf('.')
        return if (lastIndex != -1) {
            fileName.substring(0, lastIndex)
        } else {
            fileName
        }
    }
}