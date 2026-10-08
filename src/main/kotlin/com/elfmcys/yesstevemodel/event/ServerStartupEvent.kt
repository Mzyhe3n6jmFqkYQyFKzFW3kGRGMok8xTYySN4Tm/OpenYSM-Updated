package com.elfmcys.yesstevemodel.event

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.model.ServerModelManager
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents

object ServerStartupEvent {
    init {
        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            if (!YesSteveModel.isAvailable) return@register
            ServerModelManager.loadModels({ result ->
                if (!result.isSuccess) {
                    server.execute {
                        throw RuntimeException("YSM Loading Failed: ${result.errorMessage?.getString(256)}")
                    }
                }
            })
        }
    }
}
