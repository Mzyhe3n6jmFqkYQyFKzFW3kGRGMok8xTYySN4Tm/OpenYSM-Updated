package com.elfmcys.yesstevemodel.util.data

import it.unimi.dsi.fastutil.objects.*

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

    // TODO: Remove Suppress
    @Suppress("UNCHECKED_CAST")
    constructor(object2ObjectArrayMap: Object2ObjectArrayMap<K, V>) {
        val array = object2ObjectArrayMap.keys.toTypedArray()
        val array2 = object2ObjectArrayMap.values.toTypedArray()
        this.keyList = ObjectLists.unmodifiable(ObjectArrayList.wrap(array))
        this.valuesList = ObjectLists.unmodifiable(ObjectArrayList.wrap(array2))
        this.arrayMap = Object2ObjectArrayMap(array, array2)
        this.hashMap = Object2ObjectOpenHashMap(this.arrayMap)
    }

    override val size: Int
        get() = hashMap.size

    fun getKeyAt(i: Int): K = keyList[i]

    fun getKeys(): List<K> = keyList

    fun getValueAt(i: Int): V = valuesList[i]

    fun getValuesList(): List<V> = valuesList

    override fun isEmpty(): Boolean = hashMap.isEmpty()

    override fun containsKey(key: K): Boolean = hashMap.containsKey(key)

    override fun containsValue(value: V): Boolean = hashMap.containsValue(value)

    override fun get(key: K): V? = hashMap[key]

    override val keys: Set<K>
        get() = arrayMap.keys

    override val values: Collection<V>
        get() = arrayMap.values

    override val entries: Set<Map.Entry<K, V>>
        get() = arrayMap.entries
}