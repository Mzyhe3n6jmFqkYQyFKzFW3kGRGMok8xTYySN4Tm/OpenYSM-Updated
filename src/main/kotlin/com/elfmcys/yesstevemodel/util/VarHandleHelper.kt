package com.elfmcys.yesstevemodel.util

import java.lang.invoke.MethodHandles
import java.lang.invoke.VarHandle
import java.util.Optional

object VarHandleHelper {
    @JvmStatic
    fun findField(cls: Class<*>, name: String, fieldType: Class<*>): Optional<VarHandle> {
        return Optional.ofNullable(findFieldOrNull(cls, name, fieldType))
    }

    @JvmStatic
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