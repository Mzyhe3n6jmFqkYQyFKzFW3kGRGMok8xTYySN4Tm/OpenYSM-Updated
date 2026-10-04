@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package rip.ysm.algorithms

/* Luis Bodart A01635000 */
/* Mersenne Twister de 64 bits (MT19937-64) */

class MT19937(seed: Long = System.currentTimeMillis()) {
    private val mt: LongArray = LongArray(N)
    private var index: Int = N

    init {
        setSeed(seed)
    }

    private fun setSeed(seed: Long) {
        mt[0] = seed
        index = N
        for (i in 1 until N) {
            mt[i] = F * (mt[i - 1] xor (mt[i - 1] ushr (W - 2))) + i
        }
    }

    private fun twist() {
        for (i in 0 until N) {
            val x = (mt[i] and UPPER_MASK) or (mt[(i + 1) % N] and LOWER_MASK)
            var xA = x ushr 1
            if ((x and 1L) != 0L) {
                xA = xA xor A
            }
            mt[i] = mt[(i + M) % N] xor xA
        }
        index = 0
    }

    fun extract_number(): Long {
        if (index >= N) {
            twist()
        }
        var y = mt[index++]
        y = y xor ((y ushr U) and D)
        y = y xor ((y shl S) and B)
        y = y xor ((y shl T) and C)
        y = y xor (y ushr L)
        return y
    }

    fun random(): Double {
        return (extract_number() ushr 1).toDouble() / Long.MAX_VALUE.toDouble()
    }

    fun randint(x: Int): Int {
        require(x > 0) { "n debe ser mayor a 0" }
        var bits: Int
        var resultVal: Int
        do {
            bits = (extract_number() ushr (W - R)).toInt()
            resultVal = bits % x
        } while (bits - resultVal + (x - 1) < 0)
        return resultVal
    }

    fun randfloat(x: Float): Float {
        require(x > 0.0f) { "n debe ser mayor a 0.0" }
        return (random() * x).toFloat()
    }

    fun randdouble(x: Double): Double {
        require(x > 0.0) { "n debe ser mayor a 0.0" }
        return random() * x
    }

    fun randrange(min: Int, max: Int): Int {
        require(min < max) { "min debe ser menor que max" }
        return randint(max - min) + min
    }

    fun getSeed(): Long {
        return mt[0]
    }

    @Suppress("ConstPropertyName")
    companion object {
        const val W: Int = 64
        const val N: Int = 312
        const val M: Int = 156
        const val R: Int = 31
        val A: Long = 0xB5026F5AA96619E9UL.toLong()
        const val U: Int = 29
        const val S: Int = 17
        const val T: Int = 37
        const val D: Long = 0x5555555555555555L
        const val B: Long = 0x71d67fffeda60000L
        const val C: Long = -0x81120000000000L // 0xfff7eee000000000L
        const val L: Int = 43
        const val F: Long = 6364136223846793005L
        const val LOWER_MASK: Long = 0x7FFFFFFFL
        const val UPPER_MASK: Long = -0x80000000L // 0xFFFFFFFF80000000L
        const val DEFAULT_SEED: Int = 5489

        // Keep lowercase alias if needed for java compat
        const val w: Int = W

        const val n: Int = N

        const val m: Int = M

        const val r: Int = R

        @JvmField
        val a: Long = A

        const val u: Int = U

        const val s: Int = S

        const val t: Int = T

        const val d: Long = D

        const val b: Long = B

        const val c: Long = C

        const val l: Int = L

        const val f: Long = F

        const val lower_mask: Long = LOWER_MASK

        const val upper_mask: Long = UPPER_MASK

        const val default_seed: Int = DEFAULT_SEED
    }
}
