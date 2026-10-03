package com.elfmcys.yesstevemodel.geckolib3.core.molang.storage

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.PooledStringHashMap
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.PooledStringHashSet
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool

class VariableStorage : IScopedVariableStorage, IForeignVariableStorage {
    val localVariables = TempVariableStorage()
    private val scopedMap = PooledStringHashMap<VariableValueHolder>(SCOPED_INIT_CAPACITY)
    private var publicMap = PooledStringHashMap<VariableValueHolder>(0)

    override fun getScoped(address: Int): Any? {
        val valueHolder = scopedMap.computeIfAbsent(address) { VariableValueHolder() }
        return valueHolder.value
    }

    override fun setScoped(address: Int, value: Any?) {
        val valueHolder = scopedMap.computeIfAbsent(address) { VariableValueHolder() }
        valueHolder.value = value
    }

    override fun getPublic(name: Int): Any? {
        val valueHolder = publicMap.get(name)
        return valueHolder?.value
    }

    fun initialize(publicVariableNames: PooledStringHashSet?) {
        scopedMap.clear()
        if (publicVariableNames != null && !publicVariableNames.isEmpty()) {
            val newPublicMap = PooledStringHashMap<VariableValueHolder>(publicVariableNames.size)
            val it = publicVariableNames.iterator()
            while (it.hasNext()) {
                val publicVariableName = it.nextInt()
                val value = VariableValueHolder()
                scopedMap.put(publicVariableName, value)
                newPublicMap.put(publicVariableName, value)
            }
            publicMap = newPublicMap
        } else {
            publicMap = PooledStringHashMap(0)
        }
    }

    fun forEachPropertyName(action: (String) -> Unit) {
        val it = scopedMap.keys.iterator()
        while (it.hasNext()) {
            action(StringPool.getString(it.nextInt()))
        }
    }

    private class VariableValueHolder {
        var value: Any? = null
    }

    companion object {
        private const val SCOPED_INIT_CAPACITY: Int = 16
    }
}