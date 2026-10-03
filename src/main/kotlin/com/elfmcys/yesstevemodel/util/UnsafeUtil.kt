package com.elfmcys.yesstevemodel.util

import sun.misc.Unsafe
import java.lang.reflect.Field

object UnsafeUtil {
    private val UNSAFE: Unsafe = runCatching {
        val declaredField: Field = Unsafe::class.java.getDeclaredField("theUnsafe")
        declaredField.isAccessible = true
        declaredField.get(null) as Unsafe
    }.getOrElse { e ->
        throw RuntimeException("Couldn't obtain reference to sun.misc.Unsafe", e)
    }

    @JvmStatic
    fun getUnsafe(): Unsafe {
        return UNSAFE
    }
}