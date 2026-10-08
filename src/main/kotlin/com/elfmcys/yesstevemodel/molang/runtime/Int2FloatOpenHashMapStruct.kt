package com.elfmcys.yesstevemodel.molang.runtime

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.molang.runtime.binding.ValueConversions
import it.unimi.dsi.fastutil.ints.Int2FloatMap
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap

open class Int2FloatOpenHashMapStruct(
    val properties: Int2FloatOpenHashMap
) : Struct {

    override fun get(name: Int): Any {
        return properties.get(name)
    }

    override fun set(name: Int, value: Any?) {
        properties.put(name, ValueConversions.asFloat(value))
    }

    open fun merge(int2FloatMap: Int2FloatMap) {
        properties.putAll(int2FloatMap)
    }

    override fun copy(): Struct {
        val hashMapStruct = HashMapStruct(true)
        for (entry in properties.int2FloatEntrySet()) {
            hashMapStruct[entry.intKey] = entry.floatValue
        }
        return hashMapStruct
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("roaming{")
        var first = true
        for (entry in properties.int2FloatEntrySet()) {
            if (!first) {
                sb.append(", ")
            }
            first = false
            sb.append(String.format("%s=%s", StringPool.getString(entry.intKey), entry.floatValue))
        }
        sb.append("}")
        return sb.toString()
    }
}