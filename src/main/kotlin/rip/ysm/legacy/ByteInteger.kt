@file:Suppress("unused")

package rip.ysm.legacy

object ByteInteger {
    fun int2Bytes(value: Int): ByteArray {
        val b = ByteArray(4)
        for (i in 0 until 4) {
            b[3 - i] = (value ushr 8 * i).toByte()
        }
        return b
    }

    fun bytes2Int(b: ByteArray, start: Int): Int {
        var len = 4
        var sum = 0
        val end = start + len
        for (i in start until end) {
            val n = b[i].toInt() and 0xff shl --len * 8
            sum += n
        }
        return sum
    }
}