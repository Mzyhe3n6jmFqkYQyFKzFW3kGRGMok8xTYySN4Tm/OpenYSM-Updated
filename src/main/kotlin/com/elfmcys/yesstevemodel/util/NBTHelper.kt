@file:Suppress("unused")

package com.elfmcys.yesstevemodel.util

import com.elfmcys.yesstevemodel.data.NbtSave
import net.minecraft.core.BlockPos
import net.minecraft.core.UUIDUtil
import net.minecraft.core.Vec3i
import net.minecraft.nbt.*
import java.util.*

object NBTHelper {
    fun getBlockPos(tag: CompoundTag): BlockPos =
        BlockPos(tag.getIntOr("x", 0), tag.getIntOr("y", 0), tag.getIntOr("z", 0))

    fun <T, T2> getHashMap(tag: CompoundTag, value: (String, CompoundTag) -> T2, load: (T2) -> T): HashMap<String, T> {
        val newHashMap = hashMapOf<String, T>()
        for (name in tag.keySet()) newHashMap[name] = load(value(name, tag))
        return newHashMap
    }

    fun <T, T2> getHashMap(
        tag: CompoundTag,
        value: (String, CompoundTag) -> T2,
        load: (String, T2) -> T
    ): HashMap<String, T> {
        val newHashMap = hashMapOf<String, T>()
        for (name in tag.keySet()) newHashMap[name] = load(name, value(name, tag))
        return newHashMap
    }

    fun <T, T2> getHashSet(tag: ListTag, value: (Int, ListTag) -> T2, load: (T2) -> T): HashSet<T> {
        val set = hashSetOf<T>()
        for (i in tag.indices) set.add(load(value(i, tag)))
        return set
    }

    fun <T, T2> getHashSet(tag: ListTag, value: (Int, ListTag) -> T2, load: (Int, T2) -> T): HashSet<T> {
        val set = hashSetOf<T>()
        for (i in tag.indices) set.add(load(i, value(i, tag)))
        return set
    }

    fun <T, T2> getMutableList(tag: ListTag, value: (Int, ListTag) -> T2, load: (T2) -> T): MutableList<T> {
        val list = mutableListOf<T>()
        for (i in tag.indices) list.add(load(value(i, tag)))
        return list
    }

    fun <T, T2> getMutableList(tag: ListTag, value: (Int, ListTag) -> T2, load: (Int, T2) -> T): MutableList<T> {
        val list = mutableListOf<T>()
        for (i in tag.indices) list.add(load(i, value(i, tag)))
        return list
    }
}

fun Vec3i.save(): CompoundTag {
    val tag = CompoundTag()
    tag.putInt("x", x)
    tag.putInt("y", y)
    tag.putInt("z", z)
    return tag
}

inline fun <reified T> List<T>.save(): ListTag {
    val tag = ListTag()
    for (name in this) {
        when {
            T::class == String::class -> tag.add(StringTag.valueOf(name as String))
            T::class == Int::class -> tag.add(IntTag.valueOf(name as Int))
            T::class == IntArray::class -> tag.add(IntArrayTag(name as IntArray))
            T::class == Long::class -> tag.add(LongTag.valueOf(name as Long))
            T::class == LongArray::class -> tag.add(LongArrayTag(name as LongArray))
            T::class == Short::class -> tag.add(ShortTag.valueOf(name as Short))
            T::class == Byte::class -> tag.add(ByteTag.valueOf(name as Byte))
            T::class == ByteArray::class -> tag.add(ByteArrayTag(name as ByteArray))
            T::class == Float::class -> tag.add(FloatTag.valueOf(name as Float))
            T::class == Double::class -> tag.add(DoubleTag.valueOf(name as Double))
            T::class == Boolean::class -> tag.add(ByteTag.valueOf(name as Boolean))
            UUID::class.java.isAssignableFrom(T::class.java) -> tag.add(IntArrayTag(UUIDUtil.uuidToIntArray(name as UUID)))
            Vec3i::class.java.isAssignableFrom(T::class.java) -> tag.add((name as Vec3i).save())
            NbtSave::class.java.isAssignableFrom(T::class.java) -> tag.add((name as NbtSave).save())
            else -> throw ArrayIndexOutOfBoundsException("Type ${T::class} is not supported")
        }
    }
    return tag
}

inline fun <reified T> Set<T>.save(): ListTag = toList().save()

inline fun <reified T> Map<String, T>.save(): CompoundTag {
    val tag = CompoundTag()
    for ((key, value) in this) {
        when {
            T::class == String::class -> tag.put(key, StringTag.valueOf(value as String))
            T::class == Int::class -> tag.put(key, IntTag.valueOf(value as Int))
            T::class == IntArray::class -> tag.put(key, IntArrayTag(value as IntArray))
            T::class == Long::class -> tag.put(key, LongTag.valueOf(value as Long))
            T::class == LongArray::class -> tag.put(key, LongArrayTag(value as LongArray))
            T::class == Short::class -> tag.put(key, ShortTag.valueOf(value as Short))
            T::class == Byte::class -> tag.put(key, ByteTag.valueOf(value as Byte))
            T::class == ByteArray::class -> tag.put(key, ByteArrayTag(value as ByteArray))
            T::class == Float::class -> tag.put(key, FloatTag.valueOf(value as Float))
            T::class == Double::class -> tag.put(key, DoubleTag.valueOf(value as Double))
            T::class == Boolean::class -> tag.put(key, ByteTag.valueOf(value as Boolean))
            UUID::class.java.isAssignableFrom(T::class.java) -> tag.put(
                key,
                IntArrayTag(UUIDUtil.uuidToIntArray(value as UUID))
            )

            Vec3i::class.java.isAssignableFrom(T::class.java) -> tag.put(key, (value as Vec3i).save())
            NbtSave::class.java.isAssignableFrom(T::class.java) -> tag.put(key, (value as NbtSave).save())
            else -> throw ArrayIndexOutOfBoundsException("Type ${T::class} is not supported")
        }
    }
    return tag
}

@JvmName("save2")
inline fun <reified T> Map<String, List<T>>.save(): CompoundTag {
    val tag = CompoundTag()
    for ((key, value) in this) tag.put(key, value.save())
    return tag
}

@JvmName("save3")
inline fun <reified T> Map<String, Set<T>>.save(): CompoundTag {
    val tag = CompoundTag()
    for ((key, value) in this) tag.put(key, value.save())
    return tag
}

@JvmName("save4")
inline fun <reified T> Map<String, Map<String, T>>.save(): CompoundTag {
    val tag = CompoundTag()
    for ((key, value) in this) tag.put(key, value.save())
    return tag
}

fun CompoundTag.getOrNull(key: String): Tag? = if (contains(key)) get(key) else null

fun CompoundTag.getByteOrNull(key: String): Byte? = if (contains(key)) getByte(key).orElse(null) else null

fun CompoundTag.getShortOrNull(key: String): Short? = if (contains(key)) getShort(key).orElse(null) else null

fun CompoundTag.getIntOrNull(key: String): Int? = if (contains(key)) getInt(key).orElse(null) else null

fun CompoundTag.getLongOrNull(key: String): Long? = if (contains(key)) getLong(key).orElse(null) else null

fun CompoundTag.getFloatOrNull(key: String): Float? = if (contains(key)) getFloat(key).orElse(null) else null

fun CompoundTag.getDoubleOrNull(key: String): Double? = if (contains(key)) getDouble(key).orElse(null) else null

fun CompoundTag.getStringOrNull(key: String): String? = if (contains(key)) getString(key).orElse(null) else null

fun CompoundTag.getByteArrayOrNull(key: String): ByteArray? =
    if (contains(key)) getByteArray(key).orElse(null) else null

fun CompoundTag.getIntArrayOrNull(key: String): IntArray? = if (contains(key)) getIntArray(key).orElse(null) else null

fun CompoundTag.getLongArrayOrNull(key: String): LongArray? =
    if (contains(key)) getLongArray(key).orElse(null) else null

fun CompoundTag.getCompoundOrNull(key: String): CompoundTag? =
    if (contains(key)) getCompound(key).orElse(null) else null

fun CompoundTag.getListOrNull(key: String): ListTag? = if (contains(key)) getList(key).orElse(null) else null

fun CompoundTag.getBooleanOrNull(key: String): Boolean? = if (contains(key)) getBoolean(key).orElse(null) else null

fun CompoundTag.getUUIDOrNull(key: String): UUID? =
    if (contains(key)) getIntArray(key).orElse(null)
        ?.let { if (it.size == 4) UUIDUtil.uuidFromIntArray(it) else null } else null