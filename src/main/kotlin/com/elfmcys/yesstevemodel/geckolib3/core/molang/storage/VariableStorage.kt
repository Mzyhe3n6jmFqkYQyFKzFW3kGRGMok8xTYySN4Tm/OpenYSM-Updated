package com.elfmcys.yesstevemodel.geckolib3.core.molang.storage

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.PooledStringHashMap
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.PooledStringHashSet
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import java.util.function.Consumer

open class VariableStorage : IScopedVariableStorage, IForeignVariableStorage {
    val localVariables: TempVariableStorage = TempVariableStorage()
    val scopedMap: PooledStringHashMap<VariableValueHolder> = PooledStringHashMap(SCOPED_INIT_CAPACITY)
    var publicMap: PooledStringHashMap<VariableValueHolder> = PooledStringHashMap(0)

    override fun getScoped(name: Int): Any? {
        val valueHolder: VariableValueHolder = scopedMap.computeIfAbsent(name) { VariableValueHolder() }
        return valueHolder.value
    }

    override fun setScoped(name: Int, value: Any) {
        val valueHolder: VariableValueHolder = scopedMap.computeIfAbsent(name) { VariableValueHolder() }
        valueHolder.value = value
    }

    override fun getPublic(name: Int): Any? {
        val valueHolder: VariableValueHolder? = publicMap.get(name)
        return valueHolder?.value
    }

    open fun getLocalVariables(): TempVariableStorage = localVariables

    open fun initialize(publicVariableNames: PooledStringHashSet?) {
        scopedMap.clear()
        if (publicVariableNames != null && !publicVariableNames.isEmpty()) {
            val newPublicMap = PooledStringHashMap<VariableValueHolder>(publicVariableNames.size)
            for (publicVariableName in publicVariableNames) {
                val value = VariableValueHolder()
                scopedMap.put(publicVariableName, value)
                newPublicMap.put(publicVariableName, value)
            }
            publicMap = newPublicMap
        } else {
            publicMap = PooledStringHashMap(0)
        }
    }

    open fun forEachPropertyName(consumer: Consumer<String>) {
        val it = scopedMap.keys.iterator()
        while (it.hasNext()) {
            consumer.accept(StringPool.getString(it.nextInt()))
        }
    }

    open class VariableValueHolder {
        @JvmField var value: Any? = null
    }

    companion object {
        const val SCOPED_INIT_CAPACITY: Int = 16
    }
}