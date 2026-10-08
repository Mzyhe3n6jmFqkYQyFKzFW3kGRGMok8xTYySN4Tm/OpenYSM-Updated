package com.elfmcys.yesstevemodel.client.animation.molang.struct

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.molang.runtime.HashMapStruct
import com.elfmcys.yesstevemodel.molang.runtime.Struct
import com.elfmcys.yesstevemodel.molang.runtime.binding.ValueConversions
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap
import it.unimi.dsi.fastutil.ints.IntOpenHashSet

class RoamingStruct(
    private val modelHashId: Int,
    private val floatVars: Int2FloatOpenHashMap
) : Struct {
    private val varNames = IntOpenHashSet(floatVars.keys)
    private var pendingBoneData = RoamingSyncBatch(modelHashId, 4)
    private var dirty = false

    override fun get(name: Int): Any = floatVars.get(name)

    override fun set(name: Int, value: Any?) {
        val f = ValueConversions.asFloat(value)
        if (f == floatVars.put(name, f)) return
        varNames.add(name)
        if (varNames.size > MAX_VARS) return
        pendingBoneData.changedVariables().put(name, f)
        dirty = true
    }

    override fun copy(): Struct {
        val mapStruct = HashMapStruct(true)
        val it = floatVars.int2FloatEntrySet().iterator()
        while (it.hasNext()) {
            val entry = it.next()
            mapStruct[entry.intKey] = entry.floatValue
        }
        return mapStruct
    }

    fun hasPendingChanges(): Boolean = dirty

    fun consumePendingBoneData(): RoamingSyncBatch {
        val syncBatch = pendingBoneData
        pendingBoneData = RoamingSyncBatch(modelHashId, syncBatch.changedVariables().size)
        dirty = false
        return syncBatch
    }

    fun forEachVar(consumer: (String) -> Unit) {
        val it = varNames.iterator()
        while (it.hasNext()) {
            val str = StringPool.getString(it.nextInt())
            consumer(str)
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("roaming{")
        var first = true
        val it = floatVars.int2FloatEntrySet().iterator()
        while (it.hasNext()) {
            val entry = it.next()
            if (!first) {
                sb.append(", ")
            }
            first = false
            sb.append(StringPool.getString(entry.intKey)).append('=').append(entry.floatValue)
        }
        sb.append("}")
        return sb.toString()
    }

    companion object {
        const val MAX_VARS = 64
        const val MAX_VAR_NAME_LENGTH = 32
    }
}