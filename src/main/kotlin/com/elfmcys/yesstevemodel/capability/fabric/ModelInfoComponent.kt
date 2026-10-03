package com.elfmcys.yesstevemodel.capability.fabric

import com.elfmcys.yesstevemodel.capability.ModelInfoCapability
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import org.ladysnake.cca.api.v3.component.Component

class ModelInfoComponent : Component {
    val capability: ModelInfoCapability = ModelInfoCapability()

    override fun readData(input: ValueInput) =
        input.read("ModelInfo", CompoundTag.CODEC).ifPresent(capability::deserializeNBT)

    override fun writeData(output: ValueOutput) =
        output.store("ModelInfo", CompoundTag.CODEC, capability.serializeNBT())
}
