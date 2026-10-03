package com.elfmcys.yesstevemodel.audio

import com.elfmcys.yesstevemodel.config.GeneralConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.Entity

open class YSMTickableSoundInstance(
    soundEvent: SoundEvent,
    val entity: Entity
) : AbstractTickableSoundInstance(soundEvent, SoundSource.PLAYERS, SoundInstance.createUnseededRandom()), IAudioPlayer {
    @JvmField
    var targetVolume: Float = 1.0f

    init {
        x = entity.x
        y = entity.y
        z = entity.z
    }

    override fun tick() {
        val soundVolume = GeneralConfig.SOUND_VOLUME?.get()?.toFloat() ?: 100.0f
        volume = (targetVolume * soundVolume) / 100.0f
        if (entity.isRemoved) {
            stop()
            return
        }
        x = entity.x
        y = entity.y
        z = entity.z
    }

    open fun setVolume(f: Float) {
        targetVolume = f
    }

    open fun setPitch(f: Float) {
        pitch = f
    }

    open fun stopSound() {
        attenuation = SoundInstance.Attenuation.NONE
        relative = true
    }

    override fun release() {
        stop()
        Minecraft.getInstance().execute {
            Minecraft.getInstance().soundManager.stop(this)
        }
    }

    open fun setLooping(loop: Boolean) {
        looping = loop
    }
}
