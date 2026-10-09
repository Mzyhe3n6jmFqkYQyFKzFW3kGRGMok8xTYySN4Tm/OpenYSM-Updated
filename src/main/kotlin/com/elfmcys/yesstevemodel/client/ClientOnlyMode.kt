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

    @JvmStatic
    fun isForced(): Boolean = runCatching { GeneralConfig.FORCE_CLIENT_MODE.get() }.getOrDefault(false)

    @JvmStatic
    fun isActive(): Boolean = standalone || isForced()

    @JvmStatic
    fun activateStandalone() {
        if (standalone) return
        standalone = true
        Constants.LOGGER.info("No server-side mod detected, entering client-only mode.")
        ClientModelManager.enterClientOnlyMode()
    }

    @JvmStatic
    fun leaveStandalone() {
        if (!standalone || isForced()) return
        standalone = false
        Constants.LOGGER.info("Server-side mod responded late, leaving client-only mode.")
    }

    @JvmStatic
    fun reset() {
        standalone = false
        catalogLoaded = false
    }

    @JvmStatic
    fun markCatalogLoaded(): Boolean {
        if (catalogLoaded) return false
        catalogLoaded = true
        return true
    }
}
