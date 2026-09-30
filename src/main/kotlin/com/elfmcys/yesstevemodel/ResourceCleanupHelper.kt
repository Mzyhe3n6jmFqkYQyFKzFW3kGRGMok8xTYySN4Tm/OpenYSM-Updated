package com.elfmcys.yesstevemodel

import io.netty.util.internal.ObjectCleaner
import java.util.function.BiConsumer
import java.util.function.Consumer

object ResourceCleanupHelper {
    @JvmStatic
    fun <T> registerCleanup(obj: Any, t: T, consumer: Consumer<T>) {
        ObjectCleaner.register(obj) { consumer.accept(t) }
    }

    @JvmStatic
    fun <T0, T1> registerBiCleanup(obj: Any, t0: T0, t1: T1, biConsumer: BiConsumer<T0, T1>) {
        ObjectCleaner.register(obj) { biConsumer.accept(t0, t1) }
    }
}
