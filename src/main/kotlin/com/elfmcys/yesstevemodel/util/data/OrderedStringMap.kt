package com.elfmcys.yesstevemodel.util.data

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectList
import it.unimi.dsi.fastutil.objects.ObjectLists

class OrderedStringMap<K, V> : Map<K, V> {
    private val keyList: ObjectList<K>
    private val valuesList: ObjectList<V>
    private val arrayMap: Object2ObjectArrayMap<K, V>
    private val hashMap: Object2ObjectOpenHashMap<K, V>

    constructor(kArr: Array<K>, vArr: Array<V>) {
        this.keyList = ObjectLists.unmodifiable(ObjectArrayList.wrap(kArr))
        this.valuesList = ObjectLists.unmodifiable(ObjectArrayList.wrap(vArr))
        this.arrayMap = Object2ObjectArrayMap(kArr, vArr)
        this.hashMap = Object2ObjectOpenHashMap(this.arrayMap)
    }

    @Suppress("UNCHECKED_CAST")
    constructor(object2ObjectArrayMap: Object2ObjectArrayMap<K, V>) {
        val array = object2ObjectArrayMap.keys.toTypedArray()
        val array2 = object2ObjectArrayMap.values.toTypedArray()
        this.keyList = ObjectLists.unmodifiable(ObjectArrayList.wrap(array as Array<K>))
        this.valuesList = ObjectLists.unmodifiable(ObjectArrayList.wrap(array2 as Array<V>))
        this.arrayMap = Object2ObjectArrayMap(array, array2)
        this.hashMap = Object2ObjectOpenHashMap(this.arrayMap)
    }

    override val size: Int
        get() = hashMap.size

    fun getKeyAt(i: Int): K {
        return keyList[i]
    }

    fun getKeys(): List<K> {
        return keyList
    }

    fun getValueAt(i: Int): V {
        return valuesList[i]
    }

    fun getValuesList(): List<V> {
        return valuesList
    }

    override fun isEmpty(): Boolean {
        return hashMap.isEmpty()
    }

    override fun containsKey(key: K): Boolean {
        return hashMap.containsKey(key)
    }

    override fun containsValue(value: V): Boolean {
        return hashMap.containsValue(value)
    }

    override fun get(key: K): V? {
        return hashMap[key]
    }

    override val keys: Set<K>
        get() = arrayMap.keys

    override val values: Collection<V>
        get() = arrayMap.values

    override val entries: Set<Map.Entry<K, V>>
        get() = arrayMap.entries
}