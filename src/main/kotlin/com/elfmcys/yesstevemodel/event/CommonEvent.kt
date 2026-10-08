package com.elfmcys.yesstevemodel.event

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.model.ServerModelManager
import com.elfmcys.yesstevemodel.network.NetworkHandler
import rip.ysm.compat.touhoulittlemaid.TouhouMaidCompat

object CommonEvent {
    init {
        register()
    }

    private fun register() {
        if (!YesSteveModel.isAvailable) {
            Constants.LOGGER.error(YesSteveModel.errorMessage)
            return
        }
        Constants.doNothing(NetworkHandler, TouhouMaidCompat)
        nativeInit()
    }

    private fun nativeInit(): Any? {
        runCatching {
            ServerModelManager.reloadPacks()
        }.onFailure {
            throw RuntimeException(it)
        }
        return null
    }
}
