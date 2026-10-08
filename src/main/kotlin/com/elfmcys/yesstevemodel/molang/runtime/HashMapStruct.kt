package com.elfmcys.yesstevemodel.molang.runtime

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.PooledStringHashMap
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool

open class HashMapStruct : Struct {
    private val properties: PooledStringHashMap<Any?>
    private val isRightValue: Boolean

    constructor() : this(false)

    constructor(isRightValue: Boolean) {
        this.properties = PooledStringHashMap()
        this.isRightValue = isRightValue
    }

    private constructor(properties: PooledStringHashMap<Any?>) {
        this.properties = properties
        this.isRightValue = false
    }

    override fun get(name: Int): Any? = properties.get(name)

    override fun set(name: Int, value: Any?) {
        properties[name] = value
    }

    override fun copy(): Struct {
        return if (isRightValue) {
            HashMapStruct(properties)
        } else {
            HashMapStruct(PooledStringHashMap(properties))
        }
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("struct{")
        var first = true
        for (entry in properties.int2ReferenceEntrySet()) {
            if (!first) {
                builder.append(", ")
            }
            first = false
            builder.append(
                String.format(
                    "%s=%s",
                    StringPool.getString(entry.intKey),
                    entry.value?.toString() ?: "null"
                )
            )
        }
        builder.append("}")
        return builder.toString()
    }
}