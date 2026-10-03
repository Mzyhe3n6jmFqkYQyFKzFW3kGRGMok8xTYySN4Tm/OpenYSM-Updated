package com.elfmcys.yesstevemodel.audio

import com.elfmcys.yesstevemodel.client.event.ClientTickEvent
import java.util.*

object ObjectPool {
    private val pool = LinkedList<PoolEntry<NativeAudioDecoder>>()

    @Volatile
    private var activeCount: Int = 0

    @JvmStatic
    fun acquire(): NativeAudioDecoder {
        synchronized(pool) {
            if (pool.isNotEmpty()) {
                val decoder = pool.removeLast().value
                if (pool.isEmpty()) {
                    activeCount = 0
                }
                return decoder
            }
            return NativeAudioDecoder()
        }
    }

    @JvmStatic
    fun release(decoder: NativeAudioDecoder) {
        decoder.reset()
        synchronized(pool) {
            val poolEntry = PoolEntry(decoder, ClientTickEvent.getTickCount() + 200)
            if (pool.isEmpty()) {
                activeCount = poolEntry.expirationTick
            }
            pool.addLast(poolEntry)
        }
    }

    @JvmStatic
    fun cleanup() {
        if (activeCount != 0) {
            val currentTick = ClientTickEvent.getTickCount()
            if (currentTick > activeCount) {
                synchronized(pool) {
                    while (pool.isNotEmpty()) {
                        val first = pool.first()
                        if (first.expirationTick <= currentTick) {
                            first.value.destroy()
                            pool.removeFirst()
                        } else {
                            activeCount = first.expirationTick
                            return
                        }
                    }
                    activeCount = 0
                }
            }
        }
    }

    @JvmRecord
    private data class PoolEntry<T>(val value: T, val expirationTick: Int)
}
