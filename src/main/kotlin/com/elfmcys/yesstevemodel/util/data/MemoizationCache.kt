@file:Suppress("unused")

package com.elfmcys.yesstevemodel.util.data

import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function

class MemoizationCache<T, U> private constructor() {
    private val cache: MutableMap<T, U> = ConcurrentHashMap()

    private fun wrapFunction(function: Function<T, U>): Function<T, U> =
        Function { obj -> cache.computeIfAbsent(obj, function) }

    private fun wrapFunction(function: (T) -> U): (T) -> U = { obj -> cache.computeIfAbsent(obj) { function(it) } }

    companion object {
        fun <T, U> memoize(function: Function<T, U>): Function<T, U> = MemoizationCache<T, U>().wrapFunction(function)

        fun <T, U> memoize(function: (T) -> U): (T) -> U = MemoizationCache<T, U>().wrapFunction(function)
    }
}