package com.elfmcys.yesstevemodel.capability.fabric

import com.elfmcys.yesstevemodel.capability.ProjectileModelCapability
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import org.ladysnake.cca.api.v3.component.Component

class ProjectileModelComponent : Component {
    val capability: ProjectileModelCapability = ProjectileModelCapability()

    override fun readData(input: ValueInput) =
        input.read("ProjectileModel", CompoundTag.CODEC).ifPresent(capability::loadFrom)

    override fun writeData(output: ValueOutput) =
        output.store("ProjectileModel", CompoundTag.CODEC, capability.save())
}
