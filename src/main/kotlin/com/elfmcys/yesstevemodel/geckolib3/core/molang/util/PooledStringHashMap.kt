package com.elfmcys.yesstevemodel.geckolib3.core.molang.util

import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap

class PooledStringHashMap<V> : Int2ReferenceOpenHashMap<V> {
    constructor() : super()

    constructor(initialCapacity: Int) : super(initialCapacity)

    constructor(m: Map<out Int, V>) : super(m)

    operator fun get(key: String): V? {
        val intKey = StringPool.getName(key)
        return if (intKey == StringPool.NONE) null else super.get(intKey)
    }

    fun put(key: String, value: V) {
        super.put(StringPool.computeIfAbsent(key), value)
    }

    operator fun set(key: String, value: V) {
        put(key, value)
    }
}