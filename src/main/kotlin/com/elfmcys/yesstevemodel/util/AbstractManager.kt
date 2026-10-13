@file:Suppress("MemberVisibilityCanBePrivate")

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

abstract class AbstractManager<T : DeepCopy<T>>(
    fileName: String,
    newData: T,
    protected val isServerSide: Boolean,
    protected val dataPath: Path = Constants.ConfigDir.resolve(fileName),
    protected val autoSaveDelay: Duration = 5.minutes,
) {
    protected open val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    protected open var data: T = newData

    /**
     * Conflated so that a burst of [save] requests collapses into a single pending write.
     * The single consumer coroutine below serializes all writes, which prevents two threads
     * from truncating and writing the same file at the same time (the previous implementation
     * launched one write per request and corrupted/lost data when they overlapped).
     */
    private val saveRequests: Channel<Unit> = Channel(Channel.CONFLATED)
    private val writeLock: Mutex = Mutex()
    private var autoSaveJob: Job? = null

    protected open fun load() {
        if (Files.exists(dataPath)) {
            data = loadData()
        }
    }

    protected abstract fun loadData(): T

    /**
     * Serializes [saveData] into [target]. Invoked off the game thread while the write lock is held,
     * so it may block on I/O. Implementations should not create [target]'s parent directory or move
     * the file themselves; that is handled by [writeAtomically].
     */
    protected abstract fun writeTo(saveData: T, target: Path)

    /** Requests a background save. Bursts are coalesced and writes are serialized. */
    open fun save() {
        saveRequests.trySend(Unit)
    }

    /**
     * Flushes the current data synchronously. This is the only entry point that blocks the calling
     * thread, and it is meant exclusively for shutdown (so the last state reaches disk before the
     * process exits). Regular gameplay saves always go through [save] on a background dispatcher.
     */
    fun saveNow() {
        runBlocking { writeLock.withLock { writeSafely { writeAtomically(data) } } }
    }

    private inline fun writeSafely(block: () -> Unit) {
        runCatching(block).onFailure {
            Constants.LOGGER.error("Failed to save {}", dataPath.fileName, it)
        }
    }

    /**
     * Writes through a sibling temporary file and then moves it into place, so readers never
     * observe a half-written (and therefore unreadable) file.
     */
    private fun writeAtomically(saveData: T) {
        Files.createDirectories(dataPath.parent)
        val temp = dataPath.resolveSibling("${dataPath.fileName}.tmp")
        try {
            writeTo(saveData, temp)
            runCatching {
                Files.move(temp, dataPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
            }.getOrElse {
                Files.move(temp, dataPath, StandardCopyOption.REPLACE_EXISTING)
            }
        } finally {
            Files.deleteIfExists(temp)
        }
    }

    private fun startAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = scope.launch {
            while (isActive) {
                delay(autoSaveDelay)
                save()
            }
        }
    }

    init {
        load()
        // Background writer: coalesces save requests and serializes all writes.
        scope.launch {
            for (unused in saveRequests) {
                writeLock.withLock { writeSafely { writeAtomically(data.deepCopy()) } }
            }
        }
        runCatching {
            ServerLifecycleEvents.START_DATA_PACK_RELOAD.register { _, _ ->
                save()
            }
            if (isServerSide) {
                // Server-owned data is only flushed while a server is running, so the auto-save is
                // driven by the server lifecycle. These managers must be initialised before
                // SERVER_STARTING fires (see YsmEventBootstrap), otherwise the listener registers
                // too late and never runs.
                ServerLifecycleEvents.SERVER_STARTING.register {
                    startAutoSave()
                }
                ServerLifecycleEvents.SERVER_STOPPING.register {
                    autoSaveJob?.cancel()
                    saveNow()
                }
            } else {
                // Client-owned data must be saved even when no server exists, so the auto-save runs
                // for the manager's lifetime instead of waiting for SERVER_STARTING.
                startAutoSave()
                ServerLifecycleEvents.SERVER_STOPPING.register {
                    saveNow()
                }
            }
        }
    }
}
