package com.elfmcys.yesstevemodel

import io.netty.util.internal.ObjectCleaner

object ResourceCleanupHelper {
    @JvmStatic
    fun <T> registerCleanup(obj: Any, t: T, consumer: (T) -> Unit) {
        ObjectCleaner.register(obj) { consumer(t) }
    }

    @JvmStatic
    fun <T0, T1> registerBiCleanup(obj: Any, t0: T0, t1: T1, biConsumer: (T0, T1) -> Unit) {
        ObjectCleaner.register(obj) { biConsumer(t0, t1) }
    }
}
