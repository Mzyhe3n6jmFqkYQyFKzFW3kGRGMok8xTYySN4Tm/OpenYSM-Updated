package com.elfmcys.yesstevemodel

import io.netty.util.internal.ObjectCleaner

object ResourceCleanupHelper {
    @JvmStatic
    fun <T> registerCleanup(obj: Any, t: T, func: (T) -> Unit) {
        ObjectCleaner.register(obj) { func(t) }
    }

    @JvmStatic
    fun <T0, T1> registerBiCleanup(obj: Any, t0: T0, t1: T1, func: (T0, T1) -> Unit) {
        ObjectCleaner.register(obj) { func(t0, t1) }
    }
}
