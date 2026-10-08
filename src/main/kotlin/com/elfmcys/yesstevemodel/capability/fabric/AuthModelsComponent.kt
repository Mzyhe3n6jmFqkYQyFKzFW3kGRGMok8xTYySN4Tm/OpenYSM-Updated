package com.elfmcys.yesstevemodel.capability.fabric

import com.elfmcys.yesstevemodel.capability.AuthModelsCapability
import com.mojang.serialization.Codec
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import org.ladysnake.cca.api.v3.component.Component

class AuthModelsComponent : Component {
    val capability: AuthModelsCapability = AuthModelsCapability()

    override fun readData(input: ValueInput) {
        capability.clear()
        input.listOrEmpty("AuthModels", Codec.STRING).forEach(capability::addModel)
    }

    override fun writeData(output: ValueOutput) {
        val list = output.list("AuthModels", Codec.STRING)
        for (s in capability.authModels) list.add(s)
    }
}
