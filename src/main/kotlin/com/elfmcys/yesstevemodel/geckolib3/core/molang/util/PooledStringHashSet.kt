@file:Suppress("unused")

package com.elfmcys.yesstevemodel.geckolib3.core.molang.util

import it.unimi.dsi.fastutil.ints.IntOpenHashSet

class PooledStringHashSet : IntOpenHashSet {
    constructor() : super()
    constructor(initialCapacity: Int) : super(initialCapacity)
    constructor(m: Collection<String>) : super(m.map { StringPool.computeIfAbsent(it) })
    constructor(m: IntOpenHashSet) : super(m)

    fun contains(key: String): Boolean {
        val intKey = StringPool.getName(key)
        return intKey != StringPool.NONE && super.contains(intKey)
    }

    fun add(key: String): Boolean {
        return super.add(StringPool.computeIfAbsent(key))
    }
}