package com.elfmcys.yesstevemodel.util

import it.unimi.dsi.fastutil.longs.LongArrayList
import kotlin.math.min
import kotlin.math.roundToInt

class TickCounter(initialCapacity: Int, ticksPerSecond: Float) {
    private val timestamps: LongArrayList = LongArrayList(initialCapacity).apply {
        repeat(initialCapacity) {
            add(0L)
        }
    }
    private val maxCount: Int = (1000.0f / ticksPerSecond).roundToInt()
    private var windowMs: Long = 0L

    fun tryIncrement(): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime < windowMs) {
            return false
        }
        timestamps.removeLong(timestamps.size - 1)
        timestamps.add(0, currentTime)
        val j = maxCount.toLong()
        var minTime = Long.MAX_VALUE
        for (i in 0 until timestamps.size) {
            minTime = min(minTime, timestamps.getLong(i) + j * (i + 1))
        }
        windowMs = minTime
        return true
    }
}