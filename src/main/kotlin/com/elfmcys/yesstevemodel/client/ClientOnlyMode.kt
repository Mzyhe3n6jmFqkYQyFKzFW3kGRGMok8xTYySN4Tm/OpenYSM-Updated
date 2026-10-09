package com.elfmcys.yesstevemodel.client

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.config.GeneralConfig
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment

@Environment(EnvType.CLIENT)
object ClientOnlyMode {
    @Volatile
    private var standalone = false

    @Volatile
    private var catalogLoaded = false

    val isForced: Boolean
        get() = runCatching { GeneralConfig.FORCE_CLIENT_MODE.get() }.getOrDefault(false)

    val isActive: Boolean
        get() = standalone || isForced

    fun activateStandalone() {
        if (standalone) return
        standalone = true
        Constants.LOGGER.info("No server-side mod detected, entering client-only mode.")
        ClientModelManager.enterClientOnlyMode()
    }

    fun leaveStandalone() {
        if (!standalone || isForced) return
        standalone = false
        Constants.LOGGER.info("Server-side mod responded late, leaving client-only mode.")
    }

    fun reset() {
        standalone = false
        catalogLoaded = false
    }

    fun markCatalogLoaded(): Boolean {
        if (catalogLoaded) return false
        catalogLoaded = true
        return true
    }
}
