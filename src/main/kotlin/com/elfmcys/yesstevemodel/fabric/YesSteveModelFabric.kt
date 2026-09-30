package com.elfmcys.yesstevemodel.fabric

import com.elfmcys.yesstevemodel.YesSteveModel
import net.fabricmc.api.ModInitializer

class YesSteveModelFabric : ModInitializer {
    override fun onInitialize() {
        YesSteveModel.init()
    }
}
