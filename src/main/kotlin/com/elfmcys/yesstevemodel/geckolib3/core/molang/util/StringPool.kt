package com.elfmcys.yesstevemodel.geckolib3.core.molang.util

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

object StringPool {
    const val NONE: Int = Int.MIN_VALUE

    private val COUNTER = AtomicInteger(0)
    private val POOL = ConcurrentHashMap<String, Int>()
    private val MAP = ConcurrentHashMap<Int, String>()

    const val EMPTY: String = ""
    @JvmField
    val EMPTY_ID: Int = computeIfAbsent(EMPTY)

    @JvmStatic
    fun computeIfAbsent(str: String): Int {
        return POOL.computeIfAbsent(str) { k ->
            val name = COUNTER.incrementAndGet()
            MAP[name] = k
            name
        }
    }

    @JvmStatic
    fun getName(str: String): Int {
        return POOL.getOrDefault(str, NONE)
    }

    @JvmStatic
    fun getString(name: Int): String {
        return MAP.getOrDefault(name, EMPTY)
    }
}