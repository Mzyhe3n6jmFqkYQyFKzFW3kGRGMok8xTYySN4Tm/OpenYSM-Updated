package com.elfmcys.yesstevemodel.config;

import com.elfmcys.yesstevemodel.NameSpaces;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public class ModSoundEvents {

    public static final Identifier CUSTOM_SOUND_ID = NameSpaces.MOD.path("custom");

    public static final SoundEvent CUSTOM_SOUND_EVENT = SoundEvent.createFixedRangeEvent(CUSTOM_SOUND_ID, 16.0f);

    public static final Supplier<SoundEvent> CUSTOM_SOUND = () -> CUSTOM_SOUND_EVENT;

    public static void register() {
        Registry.register(BuiltInRegistries.SOUND_EVENT, CUSTOM_SOUND_ID, CUSTOM_SOUND_EVENT);
    }
}
