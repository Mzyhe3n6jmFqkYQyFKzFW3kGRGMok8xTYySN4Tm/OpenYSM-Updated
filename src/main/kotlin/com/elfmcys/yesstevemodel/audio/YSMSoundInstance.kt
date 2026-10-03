@file:Suppress("unused")

package com.elfmcys.yesstevemodel.audio

import net.fabricmc.fabric.api.client.sound.v1.FabricSoundInstance
import net.minecraft.client.Minecraft
import net.minecraft.client.sounds.AudioStream
import net.minecraft.client.sounds.SoundBufferLibrary
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.entity.Entity
import java.util.concurrent.CompletableFuture

class YSMSoundInstance(
    soundEvent: SoundEvent,
    private val streamFactory: IAudioStreamFactory,
    entity: Entity
) : YSMTickableSoundInstance(soundEvent, entity), FabricSoundInstance {
    @Volatile
    private var audioStream: IAudioStreamSupport? = null

    fun getStream(z: Boolean): CompletableFuture<AudioStream> {
        val completableFuture = CompletableFuture<AudioStream>()
        Minecraft.getInstance().execute {
            try {
                val stream = if (z) AudioStreamWrapper(streamFactory) else streamFactory.openStream()
                audioStream = stream
                completableFuture.complete(stream)
            } catch (th: Throwable) {
                completableFuture.completeExceptionally(th)
            }
        }
        return completableFuture
    }

    override fun isStopped(): Boolean {
        val stream = audioStream ?: return super.isStopped()
        if (stream.isClosed()) {
            if (!super.isStopped()) {
                super.release()
                return true
            }
            return true
        }
        return super.isStopped()
    }

    override fun getAudioStream(
        loader: SoundBufferLibrary,
        id: Identifier,
        looping: Boolean
    ): CompletableFuture<AudioStream> = getStream(looping)
}
