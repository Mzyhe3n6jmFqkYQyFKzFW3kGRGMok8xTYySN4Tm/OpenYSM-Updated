package rip.ysm.compat

import net.fabricmc.loader.api.FabricLoader

@Suppress("unused")
open class ModCompat(private vararg val modId: String) {
    val isModLoaded by lazy { modId.any { FabricLoader.getInstance().isModLoaded(it) } }

    open fun initialize() {}

    fun whenLoaded(action: () -> Unit) {
        if (!isModLoaded) return
        action()
    }

    init {
        if (isModLoaded)
            initialize()
    }
}