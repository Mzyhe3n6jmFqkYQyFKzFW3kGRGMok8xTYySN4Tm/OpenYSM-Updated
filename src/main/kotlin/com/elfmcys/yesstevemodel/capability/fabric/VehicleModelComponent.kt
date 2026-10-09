package com.elfmcys.yesstevemodel.capability.fabric

import com.elfmcys.yesstevemodel.capability.VehicleModelCapability
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import org.ladysnake.cca.api.v3.component.Component

class VehicleModelComponent : Component {
    val capability: VehicleModelCapability = VehicleModelCapability()

    override fun readData(input: ValueInput) =
        input.read("VehicleModel", CompoundTag.CODEC).ifPresent(capability::loadFrom)

    override fun writeData(output: ValueOutput) =
        output.store("VehicleModel", CompoundTag.CODEC, capability.save())
}
