package rip.ysm.algorithms

import java.nio.charset.StandardCharsets
import java.util.Arrays

open class CityHash {

    fun hash32(raw: String): Int {
        val byteArray = convertString2UTF8(raw)
        val len = byteArray.size
        if (len <= 24) {
            return if (len <= 12) {
                if (len <= 4) hash32Len0to4(byteArray) else hash32Len5to12(byteArray)
            } else {
                hash32Len13to24(byteArray)
            }
        }

        // len > 24
        var h = len
        var g = C1 * len
        var f = g
        var a0 = rotate32(fetch32(byteArray, len - 4) * C1, 17) * C2
        var a1 = rotate32(fetch32(byteArray, len - 8) * C1, 17) * C2
        var a2 = rotate32(fetch32(byteArray, len - 16) * C1, 17) * C2
        var a3 = rotate32(fetch32(byteArray, len - 12) * C1, 17) * C2
        var a4 = rotate32(fetch32(byteArray, len - 20) * C1, 17) * C2
        h = h xor a0
        h = rotate32(h, 19)
        h = h * 5 + 0xe6546b64.toInt()
        h = h xor a2
        h = rotate32(h, 19)
        h = h * 5 + 0xe6546b64.toInt()
        g = g xor a1
        g = rotate32(g, 19)
        g = g * 5 + 0xe6546b64.toInt()
        g = g xor a3
        g = rotate32(g, 19)
        g = g * 5 + 0xe6546b64.toInt()
        f += a4
        f = rotate32(f, 19)
        f = f * 5 + 0xe6546b64.toInt()
        var iters = (len - 1) / 20

        var pos = 0
        do {
            a0 = rotate32(fetch32(byteArray, pos) * C1, 17) * C2
            a1 = fetch32(byteArray, pos + 4)
            a2 = rotate32(fetch32(byteArray, pos + 8) * C1, 17) * C2
            a3 = rotate32(fetch32(byteArray, pos + 12) * C1, 17) * C2
            a4 = fetch32(byteArray, pos + 16)
            h = h xor a0
            h = rotate32(h, 18)
            h = h * 5 + 0xe6546b64.toInt()
            f += a1
            f = rotate32(f, 19)
            f *= C1
            g += a2
            g = rotate32(g, 18)
            g = g * 5 + 0xe6546b64.toInt()
            h = h xor (a3 + a1)
            h = rotate32(h, 19)
            h = h * 5 + 0xe6546b64.toInt()
            g = g xor a4
            g = java.lang.Integer.reverseBytes(g) * 5
            h += a4 * 5
            h = java.lang.Integer.reverseBytes(h)
            f += a0
            val swapValue = f
            f = g
            g = h
            h = swapValue

            pos += 20
        } while (--iters != 0)

        g = rotate32(g, 11) * C1
        g = rotate32(g, 17) * C1
        f = rotate32(f, 11) * C1
        f = rotate32(f, 17) * C1
        h = rotate32(h + g, 19)
        h = h * 5 + 0xe6546b64.toInt()
        h = rotate32(h, 17) * C1
        h = rotate32(h + f, 19)
        h = h * 5 + 0xe6546b64.toInt()
        h = rotate32(h, 17) * C1
        return h
    }

    fun hash64(byteArray: ByteArray): Long {
        return hash64(byteArray, 0, byteArray.size)
    }

    fun hash64(byteArray: ByteArray, base: Int, len: Int): Long {
        if (len <= 32) {
            return if (len <= 16) {
                hashLen0to16(byteArray, base, len)
            } else {
                hashLen17to32(byteArray, base, len)
            }
        } else if (len <= 64) {
            return hashLen33to64(byteArray, base, len)
        }

        // For strings over 64 bytes we hash the end first, and then as we
        // loop we keep 56 bytes of state: v, w, x, y, and z.
        var x = fetch64(byteArray, base + len - 40)
        var y = fetch64(byteArray, base + len - 16) + fetch64(byteArray, base + len - 56)
        var z = hashLen16(fetch64(byteArray, base + len - 48) + len, fetch64(byteArray, base + len - 24))
        var vLow: Long
        var vHi: Long
        var w0 = fetch64(byteArray, base + len - 64)
        var x0 = fetch64(byteArray, base + len - 56)
        var y0 = fetch64(byteArray, base + len - 48)
        var z0 = fetch64(byteArray, base + len - 40)
        var a = len.toLong() + w0
        var b = rotate(z + a + z0, 21)
        var c = a
        a += x0
        a += y0
        b += rotate(a, 44)
        vLow = a + z0
        vHi = b + c
        var wLow: Long
        var wHi: Long
        w0 = fetch64(byteArray, base + len - 32)
        x0 = fetch64(byteArray, base + len - 24)
        y0 = fetch64(byteArray, base + len - 16)
        z0 = fetch64(byteArray, base + len - 8)
        a = y + K1 + w0
        b = rotate(x + a + z0, 21)
        c = a
        a += x0
        a += y0
        b += rotate(a, 44)
        wLow = a + z0
        wHi = b + c
        x = x * K1 + fetch64(byteArray, base)

        // len > 64
        var remaining = (len - 1) and 63.inv()
        var pos = base
        do {
            x = rotate(x + y + vLow + fetch64(byteArray, pos + 8), 37) * K1
            y = rotate(y + vHi + fetch64(byteArray, pos + 48), 42) * K1
            x = x xor wHi
            y += vLow + fetch64(byteArray, pos + 40)
            z = rotate(z + wLow, 33) * K1
            w0 = fetch64(byteArray, pos)
            x0 = fetch64(byteArray, pos + 8)
            y0 = fetch64(byteArray, pos + 16)
            z0 = fetch64(byteArray, pos + 24)
            a = vHi * K1 + w0
            b = rotate(x + wLow + a + z0, 21)
            c = a
            a += x0
            a += y0
            b += rotate(a, 44)
            vLow = a + z0
            vHi = b + c
            w0 = fetch64(byteArray, pos + 32)
            x0 = fetch64(byteArray, pos + 40)
            y0 = fetch64(byteArray, pos + 48)
            z0 = fetch64(byteArray, pos + 56)
            a = z + wHi + w0
            b = rotate(y + fetch64(byteArray, pos + 16) + a + z0, 21)
            c = a
            a += x0
            a += y0
            b += rotate(a, 44)
            wLow = a + z0
            wHi = b + c
            val swapValue = x
            x = z
            z = swapValue
            pos += 64
            remaining -= 64
        } while (remaining != 0)
        return hashLen16(hashLen16(vLow, wLow) + shiftMix(y) * K1 + z, hashLen16(vHi, wHi) + x)
    }

    fun hash64WithSeeds(raw: ByteArray, seed0: Long, seed1: Long): Long {
        return hashLen16(hash64(raw) - seed0, seed1)
    }

    fun hash64WithSeeds(raw: ByteArray, base: Int, len: Int, seed0: Long, seed1: Long): Long {
        return hashLen16(hash64(raw, base, len) - seed0, seed1)
    }

    fun hash64WithSeed(raw: String, seed: Long): Long {
        val byteArray = convertString2UTF8(raw)
        return hash64WithSeeds(byteArray, K2, seed)
    }

    fun hash64WithSeed(raw: ByteArray, seed: Long): Long {
        return hash64WithSeeds(raw, K2, seed)
    }

    fun hash64WithSeed(raw: ByteArray, base: Int, len: Int, seed: Long): Long {
        return hash64WithSeeds(raw, base, len, K2, seed)
    }

    fun hash128(raw: String): Number128 {
        val byteArray = convertString2UTF8(raw)
        val len = byteArray.size
        return if (len >= 16) {
            hash128WithSeed(byteArray, 16, Number128(fetch64(byteArray, 0), fetch64(byteArray, 8) + K0))
        } else {
            hash128WithSeed(byteArray, 0, Number128(K0, K1))
        }
    }

    fun hash128WithSeed(raw: String, seed: Number128): Number128 {
        val byteArray = convertString2UTF8(raw)
        return hash128WithSeed(byteArray, 0, seed)
    }

    fun hash128WithSeed(byteArray: ByteArray, start: Int, seed: Number128): Number128 {
        var len = byteArray.size - start
        if (len < 128) {
            return cityMurmur(Arrays.copyOfRange(byteArray, start, byteArray.size), seed)
        }

        var v = Number128(0L, 0L)
        var w = Number128(0L, 0L)
        var x = seed.lowValue
        var y = seed.hiValue
        var z = len.toLong() * K1
        v.lowValue = rotate(y xor K1, 49) * K1 + fetch64(byteArray, start)
        v.hiValue = rotate(v.lowValue, 42) * K1 + fetch64(byteArray, start + 8)
        w.lowValue = rotate(y + z, 35) * K1 + x
        w.hiValue = rotate(x + fetch64(byteArray, start + 88), 53) * K1

        var pos = start
        do {
            x = rotate(x + y + v.lowValue + fetch64(byteArray, pos + 8), 37) * K1
            y = rotate(y + v.hiValue + fetch64(byteArray, pos + 48), 42) * K1
            x = x xor w.hiValue
            y += v.lowValue + fetch64(byteArray, pos + 40)
            z = rotate(z + w.lowValue, 33) * K1
            v = weakHashLen32WithSeeds(byteArray, pos, v.hiValue * K1, x + w.lowValue)
            w = weakHashLen32WithSeeds(byteArray, pos + 32, z + w.hiValue, y + fetch64(byteArray, pos + 16))
            var swapValue = x
            x = z
            z = swapValue
            pos += 64
            x = rotate(x + y + v.lowValue + fetch64(byteArray, pos + 8), 37) * K1
            y = rotate(y + v.hiValue + fetch64(byteArray, pos + 48), 42) * K1
            x = x xor w.hiValue
            y += v.lowValue + fetch64(byteArray, pos + 40)
            z = rotate(z + w.lowValue, 33) * K1
            v = weakHashLen32WithSeeds(byteArray, pos, v.hiValue * K1, x + w.lowValue)
            w = weakHashLen32WithSeeds(byteArray, pos + 32, z + w.hiValue, y + fetch64(byteArray, pos + 16))
            swapValue = x
            x = z
            z = swapValue
            pos += 64
            len -= 128
        } while (len >= 128)

        x += rotate(v.lowValue + z, 49) * K0
        y = y * K0 + rotate(w.hiValue, 37)
        z = z * K0 + rotate(w.lowValue, 27)
        w.lowValue *= 9
        v.lowValue *= K0

        var tailDone = 0
        while (tailDone < len) {
            tailDone += 32
            y = rotate(x + y, 42) * K0 + v.hiValue
            w.lowValue += fetch64(byteArray, pos + len - tailDone + 16)
            x = x * K0 + w.lowValue
            z += w.hiValue + fetch64(byteArray, pos + len - tailDone)
            w.hiValue += v.lowValue
            v = weakHashLen32WithSeeds(byteArray, pos + len - tailDone, v.lowValue + z, v.hiValue)
            v.lowValue *= K0
        }

        x = hashLen16(x, v.lowValue)
        y = hashLen16(y + z, w.lowValue)
        return Number128(
            hashLen16(x + v.hiValue, w.hiValue) + y,
            hashLen16(x + w.hiValue, y + v.hiValue)
        )
    }

    private fun hash32Len0to4(byteArray: ByteArray): Int {
        var b = 0
        var c = 9
        val len = byteArray.size
        for (i in 0 until len) {
            val v = byteArray[i].toInt()
            b = b * C1 + v
            c = c xor b
        }
        return fmix(mur(b, mur(len, c)))
    }

    private fun hash32Len5to12(byteArray: ByteArray): Int {
        val len = byteArray.size
        var a = len
        var b = len * 5
        var c = 9
        val d = b
        a += fetch32(byteArray, 0)
        b += fetch32(byteArray, len - 4)
        c += fetch32(byteArray, (len ushr 1) and 4)
        return fmix(mur(c, mur(b, mur(a, d))))
    }

    private fun hash32Len13to24(byteArray: ByteArray): Int {
        val len = byteArray.size
        val a = fetch32(byteArray, (len ushr 1) - 4)
        val b = fetch32(byteArray, 4)
        val c = fetch32(byteArray, len - 8)
        val d = fetch32(byteArray, len ushr 1)
        val e = fetch32(byteArray, 0)
        val f = fetch32(byteArray, len - 4)
        val h = len
        return fmix(mur(f, mur(e, mur(d, mur(c, mur(b, mur(a, h)))))))
    }

    private fun hashLen0to16(byteArray: ByteArray): Long {
        return hashLen0to16(byteArray, 0, byteArray.size)
    }

    private fun hashLen0to16(byteArray: ByteArray, base: Int, len: Int): Long {
        if (len >= 8) {
            val mul = K2 + len * 2
            val a = fetch64(byteArray, base) + K2
            val b = fetch64(byteArray, base + len - 8)
            val c = rotate(b, 37) * mul + a
            val d = (rotate(a, 25) + b) * mul
            return hashLen16(c, d, mul)
        }
        if (len >= 4) {
            val mul = K2 + len * 2
            val a = fetch32(byteArray, base).toLong() and 0xffffffffL
            return hashLen16(len.toLong() + (a shl 3), fetch32(byteArray, base + len - 4).toLong() and 0xffffffffL, mul)
        }
        if (len > 0) {
            val a = byteArray[base].toInt() and 0xff
            val b = byteArray[base + (len ushr 1)].toInt() and 0xff
            val c = byteArray[base + len - 1].toInt() and 0xff
            val y = a + (b shl 8)
            val z = len + (c shl 2)
            return shiftMix((y.toLong() * K2) xor (z.toLong() * K0)) * K2
        }
        return K2
    }

    private fun hashLen17to32(byteArray: ByteArray, base: Int, len: Int): Long {
        val mul = K2 + len * 2
        val a = fetch64(byteArray, base) * K1
        val b = fetch64(byteArray, base + 8)
        val c = fetch64(byteArray, base + len - 8) * mul
        val d = fetch64(byteArray, base + len - 16) * K2
        return hashLen16(rotate(a + b, 43) + rotate(c, 30) + d, a + rotate(b + K2, 18) + c, mul)
    }

    private fun hashLen33to64(byteArray: ByteArray, base: Int, len: Int): Long {
        val mul = K2 + len * 2
        var a = fetch64(byteArray, base) * K2
        var b = fetch64(byteArray, base + 8)
        val c = fetch64(byteArray, base + len - 24)
        val d = fetch64(byteArray, base + len - 32)
        val e = fetch64(byteArray, base + 16) * K2
        val f = fetch64(byteArray, base + 24) * 9
        val g = fetch64(byteArray, base + len - 8)
        val h = fetch64(byteArray, base + len - 16) * mul
        val u = rotate(a + g, 43) + (rotate(b, 30) + c) * 9
        val v = ((a + g) xor d) + f + 1
        val w = java.lang.Long.reverseBytes((u + v) * mul) + h
        val x = rotate(e + f, 42) + c
        val y = (java.lang.Long.reverseBytes((v + w) * mul) + g) * mul
        val z = e + f + c
        a = java.lang.Long.reverseBytes((x + z) * mul + y) + b
        b = shiftMix((z + a) * mul + d + h) * mul
        return b + x
    }

    private fun loadUnaligned64(byteArray: ByteArray, start: Int): Long {
        return (byteArray[start].toLong() and 0xFF) or
                ((byteArray[start + 1].toLong() and 0xFF) shl 8) or
                ((byteArray[start + 2].toLong() and 0xFF) shl 16) or
                ((byteArray[start + 3].toLong() and 0xFF) shl 24) or
                ((byteArray[start + 4].toLong() and 0xFF) shl 32) or
                ((byteArray[start + 5].toLong() and 0xFF) shl 40) or
                ((byteArray[start + 6].toLong() and 0xFF) shl 48) or
                ((byteArray[start + 7].toLong() and 0xFF) shl 56)
    }

    private fun loadUnaligned32(byteArray: ByteArray, start: Int): Int {
        return (byteArray[start].toInt() and 0xFF) or
                ((byteArray[start + 1].toInt() and 0xFF) shl 8) or
                ((byteArray[start + 2].toInt() and 0xFF) shl 16) or
                ((byteArray[start + 3].toInt() and 0xFF) shl 24)
    }

    private fun fetch64(byteArray: ByteArray, start: Int): Long {
        return loadUnaligned64(byteArray, start)
    }

    private fun fetch32(byteArray: ByteArray, start: Int): Int {
        return loadUnaligned32(byteArray, start)
    }

    private fun rotate(v: Long, shift: Int): Long {
        return if (shift == 0) v else ((v ushr shift) or (v shl (64 - shift)))
    }

    private fun hashLen16(u: Long, v: Long, mul: Long): Long {
        var a = (u xor v) * mul
        a = a xor (a ushr 47)
        var b = (v xor a) * mul
        b = b xor (b ushr 47)
        b *= mul
        return b
    }

    private fun hashLen16(u: Long, v: Long): Long {
        return hash128to64(Number128(u, v))
    }

    private fun hash128to64(number128: Number128): Long {
        val low = number128.lowValue
        val high = number128.hiValue
        val term1 = (low xor high) * KMUL
        val term2 = (shiftMix(term1) xor low) * KMUL
        return KMUL * shiftMix(term2)
    }

    private fun shiftMix(v: Long): Long {
        return v xor (v ushr 47)
    }

    private fun fmix(h: Int): Int {
        var res = h
        res = res xor (res ushr 16)
        res *= 0x85ebca6b.toInt()
        res = res xor (res ushr 13)
        res *= 0xc2b2ae35.toInt()
        res = res xor (res ushr 16)
        return res
    }

    private fun mur(aIn: Int, hIn: Int): Int {
        var a = aIn * C1
        a = rotate32(a, 17)
        a *= C2
        var h = hIn xor a
        h = rotate32(h, 19)
        return h * 5 + 0xe6546b64.toInt()
    }

    private fun weakHashLen32WithSeeds(w: Long, x: Long, y: Long, z: Long, aIn: Long, bIn: Long): Number128 {
        var a = aIn + w
        var b = rotate(bIn + a + z, 21)
        val c = a
        a += x
        a += y
        b += rotate(a, 44)
        return Number128(a + z, b + c)
    }

    private fun weakHashLen32WithSeeds(byteArray: ByteArray, start: Int, a: Long, b: Long): Number128 {
        return weakHashLen32WithSeeds(
            fetch64(byteArray, start),
            fetch64(byteArray, start + 8),
            fetch64(byteArray, start + 16),
            fetch64(byteArray, start + 24),
            a,
            b
        )
    }

    private fun cityMurmur(byteArray: ByteArray, seed: Number128): Number128 {
        val len = byteArray.size
        var a = seed.lowValue
        var b = seed.hiValue
        var c = 0L
        var d = 0L
        var l = len - 16
        if (l <= 0) {
            a = shiftMix(a * K1) * K1
            c = b * K1 + hashLen0to16(byteArray)
            d = shiftMix(a + (if (len >= 8) fetch64(byteArray, 0) else c))
        } else {
            c = hashLen16(fetch64(byteArray, len - 8) + K1, a)
            d = hashLen16(b + len, c + fetch64(byteArray, len - 16))
            a += d
            var pos = 0
            do {
                a = a xor (shiftMix(fetch64(byteArray, pos) * K1) * K1)
                a *= K1
                b = b xor a
                c = c xor (shiftMix(fetch64(byteArray, pos + 8) * K1) * K1)
                c *= K1
                d = d xor c
                pos += 16
                l -= 16
            } while (l > 0)
        }
        a = hashLen16(a, c)
        b = hashLen16(d, b)
        return Number128(a xor b, hashLen16(b, a))
    }

    private fun convertString2UTF8(raw: String): ByteArray {
        return raw.toByteArray(StandardCharsets.UTF_8)
    }

    class OrderIter(private val size: Int, private val isBigEdian: Boolean) {
        private var index: Int = 0

        fun hasNext(): Boolean {
            return index < size
        }

        fun next(): Int {
            return if (!isBigEdian) {
                index++
            } else {
                size - 1 - index++
            }
        }
    }

    data class Number128(var lowValue: Long = 0L, var hiValue: Long = 0L)

    companion object {
        @JvmField
        val IS_BIG_EDIAN: Boolean = System.getProperty("sun.cpu.endian") != "little"

        const val K0: Long = -0x1b6795dc71a555e9L // 0xE4986A230E5AAA17L
        const val K1: Long = -0x6e50ef7fd354da5bL // 0x91AF10802CAB25A5L
        const val K2: Long = -0x50d6318877862639L // 0xAF29CE778879D9C7L
        const val KMUL: Long = -0x21f0911f6424546fL // 0xDE0F6EE09BDBAB91L
        const val C1: Int = 0xcc9e2d51.toInt()
        const val C2: Int = 0x1b873593

        // Java compatibility fields
        @JvmField val k0: Long = K0
        @JvmField val k1: Long = K1
        @JvmField val k2: Long = K2
        @JvmField val kMul: Long = KMUL
        @JvmField val c1: Int = C1
        @JvmField val c2: Int = C2

        @JvmStatic
        fun rotate32(v: Int, shift: Int): Int {
            return if (shift == 0) v else ((v ushr shift) or (v shl (32 - shift)))
        }
    }
}
