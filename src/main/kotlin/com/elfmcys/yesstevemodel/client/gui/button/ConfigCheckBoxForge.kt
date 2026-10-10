package com.elfmcys.yesstevemodel.client.gui.button

import com.elfmcys.yesstevemodel.extensions.setAndSave
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Checkbox
import net.minecraft.network.chat.Component
import net.minecraftforge.common.ForgeConfigSpec

object ConfigCheckBoxForge {
    fun create(x: Int, y: Int, key: String, booleanValue: ForgeConfigSpec.BooleanValue): Checkbox {
        return Checkbox.builder(Component.translatable("gui.yes_steve_model.config.$key"), Minecraft.getInstance().font)
            .pos(x, y)
            .selected(booleanValue.get())
            .onValueChange { _, value -> booleanValue.setAndSave(value) }
            .build()
    }
}