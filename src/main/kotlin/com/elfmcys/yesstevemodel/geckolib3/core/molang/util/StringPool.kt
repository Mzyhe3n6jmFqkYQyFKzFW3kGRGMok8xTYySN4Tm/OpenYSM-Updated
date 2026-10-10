package com.elfmcys.yesstevemodel.geckolib3.core.molang.util

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

object StringPool {
    const val NONE: Int = Int.MIN_VALUE

    private val COUNTER = AtomicInteger(0)
    private val POOL = ConcurrentHashMap<String, Int>()
    private val MAP = ConcurrentHashMap<Int, String>()

    const val EMPTY: String = ""

    val EMPTY_ID: Int = computeIfAbsent(EMPTY)

    fun computeIfAbsent(str: String): Int = POOL.computeIfAbsent(str) { k ->
        val name = COUNTER.incrementAndGet()
        MAP[name] = k
        name
    }

    fun getName(str: String): Int = POOL.getOrDefault(str, NONE)

    fun getString(name: Int): String = MAP.getOrDefault(name, EMPTY)
}