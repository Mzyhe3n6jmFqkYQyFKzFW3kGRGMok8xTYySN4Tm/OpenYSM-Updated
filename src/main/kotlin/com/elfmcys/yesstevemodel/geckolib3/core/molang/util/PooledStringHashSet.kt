package com.elfmcys.yesstevemodel.geckolib3.core.molang.util

import it.unimi.dsi.fastutil.ints.IntOpenHashSet

open class PooledStringHashSet : IntOpenHashSet {
    constructor() : super()

    constructor(initialCapacity: Int) : super(initialCapacity)

    constructor(m: Collection<String>) : super(m.map { StringPool.computeIfAbsent(it) })

    constructor(m: IntOpenHashSet) : super(m)

    open fun contains(key: String): Boolean {
        val intKey = StringPool.getName(key)
        return if (intKey == StringPool.NONE) {
            false
        } else {
            super.contains(intKey)
        }
    }

    open fun add(key: String): Boolean {
        return super.add(StringPool.computeIfAbsent(key))
    }
}