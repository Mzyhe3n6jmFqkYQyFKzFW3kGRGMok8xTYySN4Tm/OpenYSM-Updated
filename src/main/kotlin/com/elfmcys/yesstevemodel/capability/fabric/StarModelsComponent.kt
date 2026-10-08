package com.elfmcys.yesstevemodel.capability.fabric

import com.elfmcys.yesstevemodel.capability.StarModelsCapability
import com.mojang.serialization.Codec
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import org.ladysnake.cca.api.v3.component.Component

class StarModelsComponent : Component {
    val capability: StarModelsCapability = StarModelsCapability()

    override fun readData(input: ValueInput) {
        capability.clear()
        input.listOrEmpty("StarModels", Codec.STRING).forEach(capability::addModel)
    }

    override fun writeData(output: ValueOutput) {
        val list = output.list("StarModels", Codec.STRING)
        for (s in capability.starModels) list.add(s)
    }
}
