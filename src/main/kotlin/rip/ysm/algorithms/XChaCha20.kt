package rip.ysm.algorithms

import java.nio.ByteBuffer
import java.security.InvalidKeyException

open class XChaCha20(key: ByteArray, nonce: ByteArray, rounds: Int) : ChaCha20Base() {

    init {
        if (key.size != 32) {
            throw InvalidKeyException("Key must be 32 bytes")
        }
        require(nonce.size == 24) { "Nonce must be 24 bytes" }
        this.rounds = rounds
        keySetup(key, nonce)
    }

    private fun keySetup(keyBytes: ByteArray, nonceBytes: ByteArray) {
        val key = toIntArray(keyBytes)
        val nonce = toIntArray(nonceBytes)
        val subkey = hChaCha20(key, nonce, rounds)
        System.arraycopy(SIGMA, 0, state, 0, 4)
        System.arraycopy(subkey, 0, state, 4, 8)
        state[12] = 0
        state[13] = 0
        state[14] = nonce[4]
        state[15] = nonce[5]
    }

    fun processBytes(input: ByteArray, offset: Int, length: Int): ByteArray {
        val out = ByteArray(length)
        processBytes(input, offset, out, 0, length)
        return out
    }

    fun processBytes(input: ByteArray, inOff: Int, out: ByteArray, outOff: Int, length: Int) {
        var inIdx = inOff
        var outIdx = outOff
        var len = length
        val ks = keystreamBuf
        while (len > 0) {
            processBlock(ks)
            val blockLen = minOf(64, len)
            for (i in 0 until blockLen) {
                out[outIdx + i] = (input[inIdx + i].toInt() xor ks[i].toInt()).toByte()
            }
            incrementCounter()
            inIdx += blockLen
            outIdx += blockLen
            len -= blockLen
        }
    }

    fun updateStateYSM(hash: Long): Int {
        val hashMod = java.lang.Long.remainderUnsigned(hash, 3L).toInt()
        rounds = 10 * hashMod + 10
        val lo = (hash and 0xFFFFFFFFL).toInt()
        val hi = ((hash ushr 32) and 0xFFFFFFFFL).toInt()
        for (i in 4 until 16) {
            if (i % 2 == 0) {
                state[i] = state[i] xor lo
            } else {
                state[i] = state[i] xor hi
            }
        }
        return (((hash and 0x3FL) or 0x40L) shl 6).toInt()
    }

    companion object {
        @JvmStatic
        fun hChaCha20(key: IntArray, nonce: IntArray, rounds: Int): IntArray {
            val x = IntArray(16)
            System.arraycopy(SIGMA, 0, x, 0, 4)
            System.arraycopy(key, 0, x, 4, 8)
            x[12] = nonce[0]
            x[13] = nonce[1]
            x[14] = nonce[2]
            x[15] = nonce[3]
            shuffleState(x, rounds)
            return intArrayOf(x[0], x[1], x[2], x[3], x[12], x[13], x[14], x[15])
        }

        @JvmStatic
        @Throws(InvalidKeyException::class)
        fun decryptYSM(data: ByteArray, key: ByteArray, iv: ByteArray, seed: Long): ByteArray {
            val keyIv = ByteArray(56)
            System.arraycopy(key, 0, keyIv, 0, 32)
            System.arraycopy(iv, 0, keyIv, 32, 24)

            val cityHash = CityHash()
            val hash2 = cityHash.hash64WithSeed(keyIv, seed)
            var nextRoundSize = (((hash2 and 0x3FL) or 0x40L) shl 6).toInt()
            var blockPointer = 0
            val initialRounds = 10 * java.lang.Long.remainderUnsigned(hash2, 3L).toInt() + 10
            val ctx = XChaCha20(key, iv, initialRounds)
            val result = ByteBuffer.allocate(data.size)

            while (blockPointer < data.size) {
                if (blockPointer + nextRoundSize > data.size) {
                    nextRoundSize = data.size - blockPointer
                }
                val decChunk = ctx.processBytes(data, blockPointer, nextRoundSize)
                blockPointer += nextRoundSize
                result.put(decChunk)
                if (blockPointer < data.size) {
                    val resHash = cityHash.hash64WithSeed(decChunk, seed)
                    nextRoundSize = ctx.updateStateYSM(resHash)
                }
            }
            return result.array()
        }
    }
}
