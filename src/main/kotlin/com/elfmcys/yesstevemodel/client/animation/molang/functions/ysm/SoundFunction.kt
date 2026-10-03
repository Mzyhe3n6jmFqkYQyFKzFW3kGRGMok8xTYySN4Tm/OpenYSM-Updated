package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.EntityFunction
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function.ArgumentCollection
import com.elfmcys.yesstevemodel.molang.runtime.binding.ValueConversions
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity

object SoundFunction {
    class StopSoundFunction : EntityFunction() {
        override fun eval(context: ExecutionContext<IContext<Entity>>, arguments: ArgumentCollection): Any {
            if (!context.entity().isClientSide()) return false
            val idValue = arguments.getValue(context, 0)
            val id = if (idValue is Number) {
                val num = -idValue.toInt()
                if (num > 0) return false
                num
            } else ValueConversions.asStringId(idValue)
            val audioPlayerManager =
                context.entity().getAudioPlayerManager(arguments.size() == 2 && arguments.getAsBoolean(context, 1))
            return audioPlayerManager?.stopSound(id) ?: false
        }

        override fun validateArgumentSize(size: Int): Boolean {
            return size == 1 || size == 2
        }
    }

    class StopAllSoundsFunction : EntityFunction() {
        override fun eval(context: ExecutionContext<IContext<Entity>>, arguments: ArgumentCollection): Any {
            if (!context.entity().isClientSide()) return false
            val audioPlayerManager =
                context.entity().getAudioPlayerManager(arguments.size() > 0 && arguments.getAsBoolean(context, 0))
            if (audioPlayerManager != null) {
                audioPlayerManager.stopAll()
                return true
            }
            return false
        }

        override fun validateArgumentSize(size: Int): Boolean = size <= 1
    }

    class PlaySoundFunction : EntityFunction() {
        override fun eval(context: ExecutionContext<IContext<Entity>>, arguments: ArgumentCollection): Any {
            if (!context.entity().isClientSide()) return false
            val idValue = arguments.getValue(context, 0)
            val id = if (idValue is Number) {
                val num = -idValue.toInt()
                if (num > 0) return false
                num
            } else {
                ValueConversions.asStringId(idValue)
            }
            val soundName = arguments.getAsString(context, 1) ?: return false
            if (soundName.isNotBlank()) {
                val flags = if (arguments.size() >= 3) {
                    val f = arguments.getAsInt(context, 2)
                    if (f !in 0..7) return false
                    f
                } else 0
                val audioPlayerManager = context.entity().getAudioPlayerManager((flags and 2) == 2) ?: return false
                return audioPlayerManager.playSound(
                    context.entity().geoInstance(),
                    id,
                    soundName,
                    (flags and 1) == 1
                ) { sound ->
                    if (sound == null) {
                        context.entity().logWarning("Sound not found: %s", soundName)
                        return@playSound
                    }
                    sound.setLooping((flags and 4) == 4)
                    if (arguments.size() >= 4)
                        sound.setVolume(Mth.clamp(arguments.getAsFloat(context, 3), 0.001f, 1000.0f))
                    if (arguments.size() >= 5)
                        sound.setPitch(Mth.clamp(arguments.getAsFloat(context, 4), 0.001f, 1000.0f))
                    if (context.entity().geoInstance().hasCustomTexture()) sound.stopSound()
                }
            }
            return false
        }

        override fun validateArgumentSize(size: Int): Boolean = size in 2..5
    }
}