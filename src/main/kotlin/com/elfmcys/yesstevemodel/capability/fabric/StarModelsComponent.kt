package com.elfmcys.yesstevemodel.capability.fabric

import com.elfmcys.yesstevemodel.capability.StarModelsCapability
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import org.ladysnake.cca.api.v3.component.Component

class StarModelsComponent : Component {
    val capability: StarModelsCapability = StarModelsCapability()

    override fun readData(input: ValueInput) {}

    override fun writeData(output: ValueOutput) {}
}
