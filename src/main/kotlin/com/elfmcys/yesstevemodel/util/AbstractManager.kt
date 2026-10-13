package com.elfmcys.yesstevemodel.util

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.data.DeepCopy
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

abstract class AbstractManager<T : DeepCopy<T>>(fileName: String, newData: T) {
    protected open val dataPath: Path = Constants.ConfigDir.resolve(fileName)
    protected open val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    protected open val autoSaveDelay: Duration = 5.minutes

    protected open var data: T = newData

    /**
     * Conflated so that a burst of [save] requests collapses into a single pending write.
     * The single consumer coroutine below serializes all writes, which prevents two threads
     * from truncating and writing the same file at the same time (the previous implementation
     * launched one write per request and corrupted/lost data when they overlapped).
     */
    private val saveRequests: Channel<Unit> = Channel(Channel.CONFLATED)
    private val saveLock: Mutex = Mutex()
    private var autoSaveJob: Job? = null

    protected open fun load() {
        if (!Files.exists(dataPath)) {
            saveNow()
            return
        }

        data = loadData()
    }

    protected abstract fun loadData(): T

    /** Schedules a save. Bursts are coalesced and writes are serialized. */
    open fun save() {
        saveRequests.trySend(Unit)
    }

    /** Writes the current data synchronously, waiting for any in-flight asynchronous save. */
    fun saveNow() {
        runBlocking {
            saveLock.withLock { saveNow(data) }
        }
    }

    protected abstract fun saveNow(saveData: T)

    /**
     * Writes through a sibling temporary file and then moves it into place, so readers never
     * observe a half-written (and therefore unreadable) file.
     */
    protected fun writeAtomically(write: (Path) -> Unit) {
        Files.createDirectories(dataPath.parent)
        val temp = dataPath.resolveSibling("${dataPath.fileName}.tmp")
        try {
            write(temp)
            runCatching {
                Files.move(temp, dataPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
            }.getOrElse {
                Files.move(temp, dataPath, StandardCopyOption.REPLACE_EXISTING)
            }
        } finally {
            Files.deleteIfExists(temp)
        }
    }

    init {
        load()
        scope.launch {
            for (unused in saveRequests) {
                saveLock.withLock {
                    val snapshot = data.deepCopy()
                    saveNow(snapshot)
                }
            }
        }
        runCatching {
            ServerLifecycleEvents.SERVER_STARTING.register {
                autoSaveJob?.cancel()
                autoSaveJob = scope.launch {
                    while (isActive) {
                        delay(autoSaveDelay)
                        save()
                    }
                }
            }
            ServerLifecycleEvents.SERVER_STOPPING.register {
                autoSaveJob?.cancel()
                saveNow()
            }
            ServerLifecycleEvents.START_DATA_PACK_RELOAD.register { _, _ ->
                save()
            }
        }
    }
}
