package com.elfmcys.yesstevemodel.util.data

import org.apache.commons.lang3.concurrent.LazyInitializer

open class LazySupplier<T>(private val supplier: () -> T) : LazyInitializer<T>() {
    override fun initialize(): T {
        return supplier()
    }
}