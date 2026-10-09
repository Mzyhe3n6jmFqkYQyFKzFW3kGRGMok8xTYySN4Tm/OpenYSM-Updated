package com.elfmcys.yesstevemodel.util

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.data.DeepCopy
import kotlinx.coroutines.*
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import java.nio.file.Files
import java.nio.file.Path
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

abstract class AbstractManager<T : DeepCopy<T>>(fileName: String, newData: T) {
    protected open val dataPath: Path = Constants.ConfigDir.resolve(fileName)
    protected open val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    protected open val autoSaveDelay: Duration = 5.minutes

    protected open var data: T = newData

    protected open fun load() {
        if (!Files.exists(dataPath)) {
            saveNow()
            return
        }

        data = loadData()
    }

    protected abstract fun loadData(): T
    private var autoSaveJob: Job? = null

    open fun save() {
        scope.launch {
            val snapshot = data.deepCopy()
            saveNow(snapshot)
        }
    }

    fun saveNow() = saveNow(data)

    protected abstract fun saveNow(saveData: T)

    init {
        load()
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