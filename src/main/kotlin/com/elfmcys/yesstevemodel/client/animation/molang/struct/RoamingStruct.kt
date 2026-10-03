package com.elfmcys.yesstevemodel.client.animation.molang.struct

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.molang.runtime.HashMapStruct
import com.elfmcys.yesstevemodel.molang.runtime.Struct
import com.elfmcys.yesstevemodel.molang.runtime.binding.ValueConversions
import it.unimi.dsi.fastutil.ints.Int2FloatMap
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap
import it.unimi.dsi.fastutil.ints.IntIterator
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectIterator
import java.util.function.Consumer

class RoamingStruct(
    private val modelHashId: Int,
    private val floatVars: Int2FloatOpenHashMap
) : Struct {
    private val varNames: IntOpenHashSet = IntOpenHashSet(floatVars.keys)
    private var pendingBoneData: RoamingSyncBatch = RoamingSyncBatch(modelHashId, 4)
    private var dirty: Boolean = false

    override fun getProperty(i: Int): Any {
        return floatVars.get(i)
    }

    override fun putProperty(name: Int, value: Any?) {
        val f: Float = ValueConversions.asFloat(value)
        if (f == floatVars.put(name, f)) {
            return
        }
        varNames.add(name)
        if (varNames.size > MAX_VARS) {
            return
        }
        pendingBoneData.changedVariables().put(name, f)
        dirty = true
    }

    override fun copy(): Struct {
        val mapStruct = HashMapStruct(true)
        val it: ObjectIterator<Int2FloatMap.Entry> = floatVars.int2FloatEntrySet().iterator()
        while (it.hasNext()) {
            val entry: Int2FloatMap.Entry = it.next()
            mapStruct.putProperty(entry.intKey, entry.floatValue)
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

    fun forEachVar(consumer: Consumer<String>) {
        val it: IntIterator = varNames.iterator()
        while (it.hasNext()) {
            val str: String? = StringPool.getString(it.nextInt())
            if (str != null) {
                consumer.accept(str)
            }
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("roaming{")
        var first = true
        val it: ObjectIterator<Int2FloatMap.Entry> = floatVars.int2FloatEntrySet().iterator()
        while (it.hasNext()) {
            val entry: Int2FloatMap.Entry = it.next()
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
        const val MAX_VARS: Int = 64
        const val MAX_VAR_NAME_LENGTH: Int = 32
    }
}