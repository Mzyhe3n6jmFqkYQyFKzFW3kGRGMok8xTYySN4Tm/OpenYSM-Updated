package com.elfmcys.yesstevemodel.geckolib3.core.molang.util

import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap

open class PooledStringHashMap<V> : Int2ReferenceOpenHashMap<V> {
    constructor() : super()

    constructor(initialCapacity: Int) : super(initialCapacity)

    constructor(m: Map<out Int, out V>) : super(m)

    open operator fun get(key: String): V? {
        val intKey = StringPool.getName(key)
        return if (intKey == StringPool.NONE) {
            null
        } else {
            super.get(intKey)
        }
    }

    open fun put(key: String, value: V) {
        super.put(StringPool.computeIfAbsent(key), value)
    }

    open operator fun set(key: String, value: V) {
        put(key, value)
    }
}