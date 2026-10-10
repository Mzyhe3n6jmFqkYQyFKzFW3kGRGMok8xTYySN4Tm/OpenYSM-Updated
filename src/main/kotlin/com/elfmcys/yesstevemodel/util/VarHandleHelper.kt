@file:Suppress("unused")

package com.elfmcys.yesstevemodel.util

import java.lang.invoke.MethodHandles
import java.lang.invoke.VarHandle
import java.util.*

object VarHandleHelper {
    fun findField(cls: Class<*>, name: String, fieldType: Class<*>): Optional<VarHandle> {
        return Optional.ofNullable(findFieldOrNull(cls, name, fieldType))
    }

    fun findFieldOrNull(cls: Class<*>, name: String, fieldType: Class<*>): VarHandle? {
        return runCatching {
            MethodHandles.privateLookupIn(cls, MethodHandles.lookup()).findVarHandle(cls, name, fieldType)
        }.onFailure { e ->
            if (e is ReflectiveOperationException) {
                e.printStackTrace()
            }
        }.getOrNull()
    }
}