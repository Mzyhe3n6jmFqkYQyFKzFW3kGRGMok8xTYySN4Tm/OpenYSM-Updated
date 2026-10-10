@file:Suppress("unused")

package com.elfmcys.yesstevemodel.extensions

import com.google.gson.JsonObject
import kotlin.math.min

fun JsonObject.getString(key: String, default: String): String = if (has(key)) get(key).asString else default

fun JsonObject.getBoolean(key: String, default: Boolean): Boolean =
    if (has(key)) get(key).asBoolean else default

fun JsonObject.getInt(key: String, def: Int): Int =
    if (has(key)) get(key).asInt else def

fun JsonObject.getDouble(key: String, def: Double): Double =
    if (has(key)) get(key).asDouble else def

fun JsonObject.getFloatArray(key: String, size: Int): FloatArray {
    val result = FloatArray(size)
    if (has(key)) {
        val arr = getAsJsonArray(key)
        for (i in 0 until min(arr.size(), size)) {
            result[i] = arr.get(i).asFloat
        }
    }
    return result
}