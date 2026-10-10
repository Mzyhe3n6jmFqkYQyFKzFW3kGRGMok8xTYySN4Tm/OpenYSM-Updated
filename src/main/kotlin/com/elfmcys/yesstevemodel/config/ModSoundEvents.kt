package com.elfmcys.yesstevemodel.config

import com.elfmcys.yesstevemodel.NameSpaces
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvent
import java.util.function.Supplier

object ModSoundEvents {
    val CUSTOM_SOUND_ID: Identifier = NameSpaces.MOD.path("custom")

    val CUSTOM_SOUND_EVENT: SoundEvent = SoundEvent.createFixedRangeEvent(CUSTOM_SOUND_ID, 16.0f)

    val CUSTOM_SOUND: Supplier<SoundEvent> = Supplier { CUSTOM_SOUND_EVENT }

    fun register() {
        Registry.register(BuiltInRegistries.SOUND_EVENT, CUSTOM_SOUND_ID, CUSTOM_SOUND_EVENT)
    }
}