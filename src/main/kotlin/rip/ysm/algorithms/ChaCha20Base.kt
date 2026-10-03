package rip.ysm.algorithms

import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class ChaCha20Base {
    @JvmField
    val state: IntArray = IntArray(16)

    @JvmField
    var rounds: Int = 0

    protected val workingState: IntArray = IntArray(16)
    protected val keystreamBuf: ByteArray = ByteArray(64)

    fun processBlock(): ByteArray {
        processBlock(keystreamBuf)
        return keystreamBuf
    }

    fun processBlock(out: ByteArray) {
        val ws = workingState
        System.arraycopy(state, 0, ws, 0, 16)
        shuffleState(ws, rounds)
        for (i in 0 until 16) {
            ws[i] += state[i]
        }
        for (i in 0 until 16) {
            val v = ws[i]
            val p = i shl 2
            out[p] = (v and 0xFF).toByte()
            out[p + 1] = ((v ushr 8) and 0xFF).toByte()
            out[p + 2] = ((v ushr 16) and 0xFF).toByte()
            out[p + 3] = ((v ushr 24) and 0xFF).toByte()
        }
    }

    fun incrementCounter() {
        state[12]++
        if (state[12] == 0) {
            state[13]++
        }
    }

    companion object {
        @JvmField
        val SIGMA: IntArray = toIntArray(
            byteArrayOf(101, 120, 112, 97, 110, 100, 32, 51, 50, 45, 98, 121, 116, 101, 32, 107)
        )

        @JvmStatic
        fun rotateLeft(i: Int, i2: Int): Int {
            return (i ushr -i2) or (i shl i2)
        }

        @JvmStatic
        fun quarterRound(x: IntArray, a: Int, b: Int, c: Int, d: Int) {
            x[a] += x[b]
            x[d] = rotateLeft(x[d] xor x[a], 16)
            x[c] += x[d]
            x[b] = rotateLeft(x[b] xor x[c], 12)
            x[a] += x[b]
            x[d] = rotateLeft(x[d] xor x[a], 8)
            x[c] += x[d]
            x[b] = rotateLeft(x[b] xor x[c], 7)
        }

        @JvmStatic
        fun shuffleState(x: IntArray, rounds: Int) {
            val halfRounds = rounds / 2
            for (i in 0 until halfRounds) {
                quarterRound(x, 0, 4, 8, 12)
                quarterRound(x, 1, 5, 9, 13)
                quarterRound(x, 2, 6, 10, 14)
                quarterRound(x, 3, 7, 11, 15)
                quarterRound(x, 0, 5, 10, 15)
                quarterRound(x, 1, 6, 11, 12)
                quarterRound(x, 2, 7, 8, 13)
                quarterRound(x, 3, 4, 9, 14)
            }
        }

        @JvmStatic
        fun toIntArray(bArr: ByteArray): IntArray {
            val asIntBuffer = ByteBuffer.wrap(bArr).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer()
            val iArr = IntArray(asIntBuffer.remaining())
            asIntBuffer.get(iArr)
            return iArr
        }
    }
}
