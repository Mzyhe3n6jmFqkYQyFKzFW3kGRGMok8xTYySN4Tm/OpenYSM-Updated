package com.elfmcys.yesstevemodel.audio

import com.elfmcys.yesstevemodel.config.ModSoundEvents
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.entity.Entity

class AudioPlayerManager {
    private val activePlayers = Int2ReferenceOpenHashMap<IAudioPlayer>(0)
    private val playerList = ReferenceArrayList<IAudioPlayer>(0)

    fun playSound(
        entity: AnimatableEntity<out Entity>,
        soundId: Int,
        soundName: String,
        forceReplace: Boolean,
        callback: ((YSMTickableSoundInstance?) -> Unit)?
    ): Boolean {
        val soundInstance = if (soundName.contains(":")) {
            val resourceLocationTryParse = Identifier.tryParse(soundName)
            if (resourceLocationTryParse != null) YSMTickableSoundInstance(
                SoundEvent.createVariableRangeEvent(
                    resourceLocationTryParse
                ), entity.entity
            ) else null
        } else {
            entity.getAudioStreamFactory(soundName)
                .map { audioStreamFactory ->
                    YSMSoundInstance(ModSoundEvents.CUSTOM_SOUND.get(), audioStreamFactory, entity.entity)
                }
                .orElse(null)
        }

        callback?.invoke(soundInstance)

        if (soundInstance == null) return false

        if (soundId != 0) {
            if (forceReplace) {
                val previousPlayer = activePlayers.put(soundId, soundInstance)
                if (previousPlayer != null && !previousPlayer.isStopped()) previousPlayer.release()
            } else {
                val computed = activePlayers.compute(soundId) { _, existingPlayer ->
                    if (existingPlayer == null || existingPlayer.isStopped()) soundInstance else existingPlayer
                }
                if (computed !== soundInstance) return false
            }
        } else playerList.add(soundInstance)

        Minecraft.getInstance().execute {
            Minecraft.getInstance().soundManager.play(soundInstance)
        }
        return true
    }

    fun stopSound(soundId: Int): Boolean {
        if (soundId != 0) {
            val player = activePlayers.remove(soundId)
            if (player != null) {
                player.release()
                return true
            }
        }
        return false
    }

    fun stopAll() {
        activePlayers.values.forEach(IAudioPlayer::release)
        for (iAudioPlayer in playerList) iAudioPlayer.release()
        activePlayers.clear()
        playerList.clear()
    }

    fun tick() {
        val objectIteratorFastIterator = activePlayers.int2ReferenceEntrySet().fastIterator()
        while (objectIteratorFastIterator.hasNext()) {
            val entry = objectIteratorFastIterator.next()
            if (entry.value.isStopped()) {
                objectIteratorFastIterator.remove()
            }
        }
        playerList.removeIf(IAudioPlayer::isStopped)
    }
}
