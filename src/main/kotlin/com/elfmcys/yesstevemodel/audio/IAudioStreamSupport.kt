package com.elfmcys.yesstevemodel.audio

import net.minecraft.client.sounds.AudioStream

interface IAudioStreamSupport : AudioStream {
    fun isClosed(): Boolean
}
