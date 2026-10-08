package com.elfmcys.yesstevemodel.molang.runtime.binding

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.molang.parser.ast.StringExpression

object ValueConversions {
    @JvmStatic
    fun asBoolean(obj: Any?): Boolean {
        if (obj == null) return false
        if (obj is Boolean) return obj
        if (obj is Number) {
            val f = obj.toFloat()
            return !f.isNaN() && f != 0.0f
        }
        return true
    }

    @JvmStatic
    fun asFloat(obj: Any?): Float {
        if (obj == null) return 0.0f
        if (obj is Number) {
            val f = obj.toFloat()
            return if (!f.isNaN()) f else 0.0f
        }
        if (obj is Boolean) {
            return if (obj) 1.0f else 0.0f
        }
        return 1.0f
    }

    @JvmStatic
    fun asInt(obj: Any?): Int {
        if (obj == null) return 0
        if (obj is Number) {
            return obj.toInt()
        }
        if (obj is Boolean) {
            return if (obj) 1 else 0
        }
        return 1
    }

    @JvmStatic
    fun asDouble(obj: Any?): Double {
        if (obj == null) return 0.0
        if (obj is Number) {
            val d = obj.toDouble()
            return if (!d.isNaN()) d else obj.toDouble()
        }
        if (obj is Boolean) {
            return if (obj) 1.0 else 0.0
        }
        return 1.0
    }

    @JvmStatic
    fun asString(obj: Any?): String? {
        if (obj is StringExpression) {
            return obj.name
        }
        if (obj is String) {
            return obj
        }
        return null
    }

    @JvmStatic
    fun asStringId(obj: Any?): Int {
        if (obj is StringExpression) {
            return obj.path
        }
        if (obj is String) {
            return StringPool.computeIfAbsent(obj)
        }
        return StringPool.EMPTY_ID
    }
}