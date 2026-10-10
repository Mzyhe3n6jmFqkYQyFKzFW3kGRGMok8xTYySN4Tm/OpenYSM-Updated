@file:Suppress("unused")

package rip.ysm.security

import com.elfmcys.yesstevemodel.Constants
import io.netty.buffer.Unpooled
import rip.ysm.algorithms.CityHash
import rip.ysm.algorithms.MT19937
import rip.ysm.algorithms.XChaCha20
import rip.ysm.algorithms.YsmZstd
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.*

object YsmCrypt {
    private const val SEED_PACKET_VERIFICATION: Long = -0x119059c2a8f42885L // 0xEE6FA63D570BD77BL
    private const val SEED_KEY_DERIVATION: Long = -0x2fe8344584a2ca7fL // 0xD017CBBA7B5D3581L
    private const val SEED_FILE_VERIFICATION: Long = -0x61aa66247f3983d7L // 0x9E5599DB80C67C29L
    private const val SEED_RES_VERIFICATION: Long = -0x59d4e5d3bc7bd43dL // 0xA62B1A2C43842BC3L
    private const val SEED_CACHE_DECRYPTION: Long = -0x2e3c2e2ec5668ad5L // 0xD1C3D1D13A99752BL
    const val SEED_CACHE_VERIFICATION: Long = -0xcb9baee0c5ddd9fL // 0xF346451E53A22261L

    private val theRandom = SecureRandom()

    val publicKey: ByteArray = byteArrayOf(
        0x0F,
        0xC7.toByte(),
        0x7E,
        0xF3.toByte(),
        0xF4.toByte(),
        0xB8.toByte(),
        0x35,
        0x3A,
        0xA2.toByte(),
        0xBA.toByte(),
        0x7F,
        0xD3.toByte(),
        0x17,
        0x79,
        0x46,
        0x8E.toByte(),
        0x65,
        0x42,
        0xD0.toByte(),
        0x98.toByte(),
        0x8A.toByte(),
        0x9B.toByte(),
        0xB0.toByte(),
        0x19,
        0x80.toByte(),
        0x4F,
        0x81.toByte(),
        0x56,
        0x36,
        0x6A,
        0x12,
        0x62,
        0xBE.toByte(),
        0x0E,
        0xE5.toByte(),
        0xAD.toByte(),
        0x47,
        0x01,
        0xD4.toByte(),
        0x5E,
        0xE4.toByte(),
        0xEB.toByte(),
        0xFB.toByte(),
        0x36,
        0xCB.toByte(),
        0x47,
        0x42,
        0x98.toByte(),
        0xF9.toByte(),
        0xE5.toByte(),
        0x7A,
        0x5C,
        0x3C,
        0xDB.toByte(),
        0x2C,
        0x76
    )

    data class EncryptedPacket(
        val data: ByteArray,
        val nextKey: ByteArray?
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is EncryptedPacket) return false

            if (!data.contentEquals(other.data)) return false
            when {
                nextKey != null -> {
                    if (other.nextKey == null) return false
                    if (!nextKey.contentEquals(other.nextKey)) return false
                }

                other.nextKey != null -> return false
            }

            return true
        }

        override fun hashCode(): Int {
            var result = data.contentHashCode()
            result = 31 * result + (nextKey?.contentHashCode() ?: 0)
            return result
        }
    }

    fun calculateModelHashes(modelHashStr: String, serverKey: ByteArray): LongArray {
        val data = modelHashStr.toByteArray(StandardCharsets.UTF_8)
        val xored = mt19937Xor(data, serverKey, SEED_KEY_DERIVATION)
        val ch = CityHash()
        val hash1 = ch.hash64WithSeed(xored, SEED_CACHE_VERIFICATION)
        val hash2 = ch.hash64WithSeed(xored, SEED_CACHE_DECRYPTION)
        return longArrayOf(hash1, hash2)
    }

    @Throws(Exception::class)
    fun encryptServerCache(clearText: ByteArray, serverKey: ByteArray, hash1: Long, hash2: Long): ByteArray {
        return encryptServerCache(clearText, 0, clearText.size, serverKey, hash1, hash2)
    }

    @Throws(Exception::class)
    fun encryptServerCache(
        clearText: ByteArray,
        clearOffset: Int,
        clearLength: Int,
        serverKey: ByteArray,
        hash1: Long,
        hash2: Long
    ): ByteArray {
        val zstdData = YsmZstd.compress(clearText, clearOffset, clearLength)
        val paddingLength = 16 + theRandom.nextInt(112)
        val randomTop6Bits = theRandom.nextInt(64) shl 10
        val headerWord = paddingLength and 0x3FF or randomTop6Bits
        val payloadToEncrypt = ByteArray(2 + paddingLength + zstdData.size)
        payloadToEncrypt[0] = (headerWord and 0xFF).toByte()
        payloadToEncrypt[1] = (headerWord shr 8 and 0xFF).toByte()
        val padding = ByteArray(paddingLength)
        theRandom.nextBytes(padding)
        System.arraycopy(padding, 0, payloadToEncrypt, 2, paddingLength)
        System.arraycopy(zstdData, 0, payloadToEncrypt, 2 + paddingLength, zstdData.size)
        val chachaKeyS = serverKey.copyOfRange(0, 32)
        val chachaIvS = serverKey.copyOfRange(32, 56)
        mt19937XorInPlace(payloadToEncrypt, serverKey, SEED_KEY_DERIVATION)
        val encryptedPayload = modifiedChaChaEncrypt(payloadToEncrypt, chachaKeyS, chachaIvS, SEED_CACHE_DECRYPTION)
        YSMByteBuf(Unpooled.buffer()).use { headerBuf ->
            headerBuf.writeVarInt(1)
            headerBuf.writeVarInt(0)
            headerBuf.writeVarInt(0)
            headerBuf.writeVarInt(0)
            headerBuf.writeVarInt(32) // format
            headerBuf.writeVarInt(0)
            headerBuf.writeVarInt(0)
            headerBuf.writeVarInt(0)
            headerBuf.writeVarInt(0)

            val headerLen = headerBuf.rawBuf.readableBytes()
            val finalPayloadLen = headerLen + encryptedPayload.size
            val finalBuf = ByteBuffer.allocate(finalPayloadLen + 8).order(ByteOrder.LITTLE_ENDIAN)

            headerBuf.rawBuf.readBytes(finalBuf.array(), 0, headerLen)
            finalBuf.position(headerLen)
            finalBuf.put(encryptedPayload)

            val ch = CityHash()
            val calculatedHash = ch.hash64WithSeed(finalBuf.array(), 0, finalPayloadLen, SEED_CACHE_VERIFICATION)
            val realHash = calculatedHash xor hash1 xor hash2 // 签名

            finalBuf.putLong(realHash)

            return finalBuf.array()
        }
    }

    fun verifyServerCache(cacheData: ByteArray, hash1: Long, hash2: Long): Boolean {
        if (cacheData.size < 8) return false
        val payloadEnd = cacheData.size - 8
        val fileSignature = ByteBuffer.wrap(cacheData, payloadEnd, 8).order(ByteOrder.LITTLE_ENDIAN).long
        val ch = CityHash()
        val calculatedHash = ch.hash64WithSeed(cacheData, 0, payloadEnd, SEED_CACHE_VERIFICATION)
        val expectedSignature = calculatedHash xor hash1 xor hash2
        return fileSignature == expectedSignature
    }

    @Throws(Exception::class)
    fun encryptYsmFile(rawClearText: ByteArray): ByteArray {
        val key = ByteArray(32)
        val iv = ByteArray(24)
        theRandom.nextBytes(key)
        theRandom.nextBytes(iv)

        val keyIv = ByteArray(56)
        System.arraycopy(key, 0, keyIv, 0, 32)
        System.arraycopy(iv, 0, keyIv, 32, 24)

        val zstdData = YsmZstd.compress(rawClearText)
        val paddingLength = 16 + theRandom.nextInt(112)
        val randomTop6Bits = theRandom.nextInt(64) shl 10
        val headerWord = paddingLength and 0x3FF or randomTop6Bits
        val payloadToEncrypt = ByteArray(2 + paddingLength + zstdData.size)
        payloadToEncrypt[0] = (headerWord and 0xFF).toByte()
        payloadToEncrypt[1] = (headerWord shr 8 and 0xFF).toByte()
        val padding = ByteArray(paddingLength)
        theRandom.nextBytes(padding)
        System.arraycopy(padding, 0, payloadToEncrypt, 2, paddingLength)
        System.arraycopy(zstdData, 0, payloadToEncrypt, 2 + paddingLength, zstdData.size)
        val xoredData = mt19937Xor(payloadToEncrypt, keyIv, SEED_KEY_DERIVATION)
        val encryptedBinaryData = modifiedChaChaEncrypt(xoredData, key, iv, SEED_RES_VERIFICATION)
        val prefix = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte(), 0x59, 0x53, 0x47, 0x50)
        val data = "".toByteArray(StandardCharsets.UTF_8)

        val headerBytes = ByteArray(prefix.size + data.size)
        System.arraycopy(prefix, 0, headerBytes, 0, prefix.size)
        System.arraycopy(data, 0, headerBytes, prefix.size, data.size)

        val totalSizeWithoutHash = headerBytes.size + 1 + 4 + encryptedBinaryData.size + 56
        val fileBuf = ByteBuffer.allocate(totalSizeWithoutHash + 8).order(ByteOrder.LITTLE_ENDIAN)

        fileBuf.put(headerBytes)
        fileBuf.put(0x00.toByte()) // terminator

        fileBuf.putInt(3)
        fileBuf.put(encryptedBinaryData)
        fileBuf.put(key)
        fileBuf.put(iv)

        val ch = CityHash()
        val fileHash = ch.hash64WithSeed(fileBuf.array(), 0, totalSizeWithoutHash, SEED_FILE_VERIFICATION)
        fileBuf.putLong(fileHash)

        return fileBuf.array()
    }

    @Throws(Exception::class)
    fun transcodeServerDataToClientCache(
        serverData: ByteArray,
        serverKey: ByteArray,
        clientKey: ByteArray,
        hash1: Long,
        hash2: Long
    ): ByteArray {
        YSMByteBuf(Unpooled.wrappedBuffer(serverData)).use { buf ->
            val headerStart = buf.rawBuf.readerIndex()
            if (buf.readVarInt() != 1) throw RuntimeException("Invalid YSM cache format")
            buf.readVarInt()
            buf.readVarInt()
            buf.readVarInt()
            buf.readVarInt() // format
            buf.readVarInt()
            buf.readVarInt()
            buf.readVarInt()
            buf.readVarInt()

            val headerEnd = buf.rawBuf.readerIndex()
            val payloadEnd = serverData.size - 8
            if (payloadEnd <= headerEnd) {
                throw RuntimeException("Invalid server payload size!")
            }
            val headerLen = headerEnd - headerStart

            // packet shell
            val chachaKeyS = serverKey.copyOfRange(0, 32)
            val chachaIvS = serverKey.copyOfRange(32, 56)

            val plainText = modifiedChaChaDecrypt(
                serverData,
                headerEnd,
                payloadEnd - headerEnd,
                chachaKeyS,
                chachaIvS,
                SEED_CACHE_DECRYPTION
            )
            mt19937XorInPlace(plainText, serverKey, SEED_KEY_DERIVATION)

            // local shell
            val chachaKeyC = clientKey.copyOfRange(0, 32)
            val chachaIvC = clientKey.copyOfRange(32, 56)

            mt19937XorInPlace(plainText, clientKey, SEED_KEY_DERIVATION)
            val clientEncryptedPayload = modifiedChaChaEncrypt(plainText, chachaKeyC, chachaIvC, SEED_CACHE_DECRYPTION)

            val finalPayloadLen = headerLen + clientEncryptedPayload.size
            val finalBuf = ByteBuffer.allocate(finalPayloadLen + 8).order(ByteOrder.LITTLE_ENDIAN)
            finalBuf.put(serverData, headerStart, headerLen)
            finalBuf.put(clientEncryptedPayload)

            val ch = CityHash()
            val calculatedHash = ch.hash64WithSeed(finalBuf.array(), 0, finalPayloadLen, SEED_CACHE_VERIFICATION)
            val realHash = calculatedHash xor hash1 xor hash2

            finalBuf.putLong(realHash)
            return finalBuf.array()
        }
    }

    @Throws(Exception::class)
    private fun modifiedChaChaEncrypt(plainText: ByteArray, key: ByteArray, iv: ByteArray, seed: Long): ByteArray {
        val keyIv = ByteArray(56)
        System.arraycopy(key, 0, keyIv, 0, 32)
        System.arraycopy(iv, 0, keyIv, 32, 24)

        val ch = CityHash()
        val hash2 = ch.hash64WithSeed(keyIv, seed)

        var nextRoundSize = (hash2 and 0x3FL or 0x40L shl 6).toInt()
        val rounds = (10 * java.lang.Long.remainderUnsigned(hash2, 3) + 10).toInt()

        val ctx = XChaCha20(key, iv, rounds)
        val result = ByteArray(plainText.size)
        var blockPointer = 0

        while (blockPointer < plainText.size) {
            if (blockPointer + nextRoundSize > plainText.size) {
                nextRoundSize = plainText.size - blockPointer
            }

            ctx.processBytes(plainText, blockPointer, result, blockPointer, nextRoundSize)
            blockPointer += nextRoundSize

            if (blockPointer < plainText.size) {
                val resHash = ch.hash64WithSeed(plainText, blockPointer - nextRoundSize, nextRoundSize, seed)
                nextRoundSize = ctx.updateStateYSM(resHash)
            }
        }
        return result
    }

    @Throws(Exception::class)
    fun decryptYsmFile(fileData: ByteArray): ByteArray {
        if (fileData.size < 8 + 24 + 32 + 8) {
            throw RuntimeException("Invalid YSM file: File too short.")
        }

        var headerLength = 0
        while (headerLength < fileData.size && fileData[headerLength].toInt() != 0x00) {
            headerLength++
        }

        val tailOffset = fileData.size - 64
        val key = fileData.copyOfRange(tailOffset, tailOffset + 32)
        val iv = fileData.copyOfRange(tailOffset + 32, tailOffset + 56)
        val fileHash = ByteBuffer.wrap(fileData, tailOffset + 56, 8).order(ByteOrder.LITTLE_ENDIAN).long

        val ch = CityHash()
        val calculatedHash = ch.hash64WithSeed(fileData, 0, fileData.size - 8, SEED_FILE_VERIFICATION)
        if (calculatedHash != fileHash) {
            throw RuntimeException("Corrupted YSM file: File hash mismatch.")
        }

        var ptrBinaryData = headerLength + 1
        val crypto = ByteBuffer.wrap(fileData, ptrBinaryData, 4).order(ByteOrder.LITTLE_ENDIAN).int
        if (crypto != 3) {
            throw RuntimeException("Invalid YSM file: Crypto version is not 3.")
        }
        ptrBinaryData += 4

        val chachaDecrypted =
            modifiedChaChaDecrypt(fileData, ptrBinaryData, tailOffset - ptrBinaryData, key, iv, SEED_RES_VERIFICATION)

        val keyIv = ByteArray(56)
        System.arraycopy(key, 0, keyIv, 0, 32)
        System.arraycopy(iv, 0, keyIv, 32, 24)
        mt19937XorInPlace(chachaDecrypted, keyIv, SEED_KEY_DERIVATION)

        val n = chachaDecrypted[0].toInt() and 0xFF or (chachaDecrypted[1].toInt() and 0xFF shl 8) and 0x3FF

        val zstdOffset = 2 + n
        return YsmZstd.decompress(chachaDecrypted, zstdOffset, chachaDecrypted.size - zstdOffset)
    }

    @Throws(Exception::class)
    private fun modifiedChaChaDecrypt(data: ByteArray, key: ByteArray, iv: ByteArray, seed: Long): ByteArray {
        return modifiedChaChaDecrypt(data, 0, data.size, key, iv, seed)
    }

    @Throws(Exception::class)
    private fun modifiedChaChaDecrypt(
        data: ByteArray,
        dataOff: Int,
        dataLen: Int,
        key: ByteArray,
        iv: ByteArray,
        seed: Long
    ): ByteArray {
        val keyIv = ByteArray(56)
        System.arraycopy(key, 0, keyIv, 0, 32)
        System.arraycopy(iv, 0, keyIv, 32, 24)

        val ch = CityHash()
        val hash2 = ch.hash64WithSeed(keyIv, seed)

        var nextRoundSize = (hash2 and 0x3FL or 0x40L shl 6).toInt()
        val rounds = (10 * java.lang.Long.remainderUnsigned(hash2, 3) + 10).toInt()

        val ctx = XChaCha20(key, iv, rounds)

        val result = ByteArray(dataLen)
        var blockPointer = 0

        while (blockPointer < dataLen) {
            if (blockPointer + nextRoundSize > dataLen) {
                nextRoundSize = dataLen - blockPointer
            }
            ctx.processBytes(data, dataOff + blockPointer, result, blockPointer, nextRoundSize)
            blockPointer += nextRoundSize

            if (blockPointer < dataLen) {
                val resHash = ch.hash64WithSeed(result, blockPointer - nextRoundSize, nextRoundSize, seed)
                nextRoundSize = ctx.updateStateYSM(resHash)
            }
        }

        return result
    }

    @Throws(Exception::class)
    fun decrypt(packet: ByteArray, key: ByteArray): ByteArray {
        if (packet.size <= 11) throw RuntimeException("Packet too short!")

        val payloadLen = packet.size - 8
        val packetHash = ByteBuffer.wrap(packet, payloadLen, 8).order(ByteOrder.LITTLE_ENDIAN).long

        val ch = CityHash()
        val calculatedHash = ch.hash64WithSeed(packet, 0, payloadLen, SEED_PACKET_VERIFICATION)
        if (calculatedHash != packetHash) {
            Constants.LOGGER.warn("Integrity compromised: {}", Base64.getEncoder().encodeToString(packet))
        }

        val xoredData = mt19937Xor(packet, 0, payloadLen, key, SEED_KEY_DERIVATION)

        val chachaKey = key.copyOfRange(0, 32)
        val chachaIv = key.copyOfRange(32, 56)
        val chacha = XChaCha20(chachaKey, chachaIv, 30)

        chacha.processBytes(xoredData, 0, xoredData, 0, xoredData.size)
        return xoredData
    }

    @Throws(Exception::class)
    fun encrypt(payload: ByteArray, currentKeyIv: ByteArray, appendNextKey: Boolean): EncryptedPacket {
        val fullPlaintext: ByteArray
        var nextKeyIv: ByteArray? = null

        if (appendNextKey) {
            nextKeyIv = ByteArray(56)
            theRandom.nextBytes(nextKeyIv)
            fullPlaintext = ByteArray(payload.size + 56)
            System.arraycopy(payload, 0, fullPlaintext, 0, payload.size)
            System.arraycopy(nextKeyIv, 0, fullPlaintext, payload.size, 56)
        } else {
            fullPlaintext = payload
        }

        val key = currentKeyIv.copyOfRange(0, 32)
        val iv = currentKeyIv.copyOfRange(32, 56)
        val step1Encrypted = XChaCha20(key, iv, 30).processBytes(fullPlaintext, 0, fullPlaintext.size)
        val step2Xorred = mt19937Xor(step1Encrypted, currentKeyIv, SEED_KEY_DERIVATION)

        val hash = CityHash().hash64WithSeed(step2Xorred, SEED_PACKET_VERIFICATION)

        val finalPacket = ByteBuffer.allocate(step2Xorred.size + 8).order(ByteOrder.LITTLE_ENDIAN)
        finalPacket.put(step2Xorred)
        finalPacket.putLong(hash)

        return EncryptedPacket(finalPacket.array(), nextKeyIv)
    }

    private fun mt19937Xor(data: ByteArray, currentKeyIv: ByteArray, seedDerivation: Long): ByteArray {
        return mt19937Xor(data, 0, data.size, currentKeyIv, seedDerivation)
    }

    private fun mt19937Xor(
        data: ByteArray,
        offset: Int,
        length: Int,
        currentKeyIv: ByteArray,
        seedDerivation: Long
    ): ByteArray {
        val mtSeed = CityHash().hash64WithSeed(currentKeyIv, seedDerivation)
        val mt = MT19937(mtSeed)
        val result = ByteArray(length)

        var i = 0
        while (i < length) {
            val rnd = mt.extract_number()
            var j = 0
            while (j < 8 && i < length) {
                val keystreamByte = (rnd ushr j * 8 and 0xFF).toByte()
                result[i] = (data[offset + i].toInt() xor keystreamByte.toInt()).toByte()
                i++
                j++
            }
        }
        return result
    }

    private fun mt19937XorInPlace(data: ByteArray, currentKeyIv: ByteArray, seedDerivation: Long) {
        val mtSeed = CityHash().hash64WithSeed(currentKeyIv, seedDerivation)
        val mt = MT19937(mtSeed)

        var i = 0
        while (i < data.size) {
            val rnd = mt.extract_number()
            var j = 0
            while (j < 8 && i < data.size) {
                val keystreamByte = (rnd ushr j * 8 and 0xFF).toByte()
                data[i] = (data[i].toInt() xor keystreamByte.toInt()).toByte()
                i++
                j++
            }
        }
    }

    @Throws(Exception::class)
    fun read(cacheFileData: ByteArray, clientKey: ByteArray): ByteArray {
        YSMByteBuf(Unpooled.wrappedBuffer(cacheFileData)).use { buf ->
            buf.readVarInt()
            buf.readVarInt()
            buf.readVarInt()
            buf.readVarInt()
            buf.readVarInt()
            buf.readVarInt()
            buf.readVarInt()
            buf.readVarInt()
            buf.readVarInt()
            val headerEnd = buf.rawBuf.readerIndex()

            val payloadEnd = cacheFileData.size - 8
            if (payloadEnd <= headerEnd) {
                throw RuntimeException("Cache file is too small or corrupted!")
            }

            val chachaKeyC = clientKey.copyOfRange(0, 32)
            val chachaIvC = clientKey.copyOfRange(32, 56)

            val plainText = modifiedChaChaDecrypt(
                cacheFileData,
                headerEnd,
                payloadEnd - headerEnd,
                chachaKeyC,
                chachaIvC,
                SEED_CACHE_DECRYPTION
            )
            mt19937XorInPlace(plainText, clientKey, SEED_KEY_DERIVATION)

            val n = plainText[0].toInt() and 0xFF or (plainText[1].toInt() and 0xFF shl 8) and 0x3FF
            val zstdOffset = 2 + n

            return YsmZstd.decompress(plainText, zstdOffset, plainText.size - zstdOffset)
        }
    }
}