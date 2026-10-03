package com.elfmcys.yesstevemodel.geckolib3.util

class InterpolationLookup<T>(
    private val keyframes: List<T>,
    val startTime: Float,
    private val timeProvider: FrameTimeProvider<T>
) {
    val endTime: Float = timeProvider.apply(keyframes[keyframes.size - 1])
    private var currentIndex: Int = 0
    private var f3: Float = startTime
    private var f4: Float = timeProvider.apply(keyframes[0])

    init {
        if (startTime > f4 || startTime > endTime || f4 > endTime) {
            throw IllegalArgumentException()
        }
    }

    fun getAtTime(f: Float): T {
        if (keyframes.size == 1) {
            return keyframes[0]
        }
        if (f < f3) {
            val t: T = keyframes[0]
            if (currentIndex == 0) {
                return t
            }
            currentIndex = 0
            f3 = startTime
            f4 = timeProvider.apply(t)
            if (f >= f4) {
                return getAtTime(f)
            }
            return t
        }
        if (f == f3 || f < f4 || f4 == endTime) {
            return keyframes[currentIndex]
        }
        if (f >= endTime) {
            val t2: T = keyframes[keyframes.size - 1]
            val fApply: Float = timeProvider.apply(keyframes[keyframes.size - 2])
            val fApply2: Float = timeProvider.apply(t2)
            if (fApply > fApply2) {
                throw IllegalArgumentException()
            }
            currentIndex = keyframes.size - 1
            f3 = fApply
            f4 = fApply2
            return t2
        }
        var f2: Float = f4
        var i: Int = currentIndex + 1
        while (true) {
            val t3: T = keyframes[i]
            val fApply3: Float = timeProvider.apply(t3)
            if (f2 > fApply3) {
                throw IllegalArgumentException()
            }
            if (f < fApply3) {
                currentIndex = i
                f3 = f2
                f4 = fApply3
                return t3
            }
            f2 = fApply3
            i++
        }
    }

    fun interface FrameTimeProvider<T> {
        fun apply(t: T): Float
    }
}