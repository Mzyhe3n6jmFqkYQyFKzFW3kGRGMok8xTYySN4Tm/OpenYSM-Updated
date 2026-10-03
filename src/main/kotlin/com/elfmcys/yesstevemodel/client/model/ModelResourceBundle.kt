package com.elfmcys.yesstevemodel.client.model

import com.elfmcys.yesstevemodel.audio.AudioTrackData
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap

open class ModelResourceBundle(
    val soundEffects: Map<String, AudioTrackData>,
    val functions: Object2ReferenceOpenHashMap<String, IValue>,
    val events: Object2ReferenceOpenHashMap<String, MutableList<IValue>>,
    val translations: Map<String, Map<String, String>>
) {
    val metadata: Map<String, Map<String, String>> get() = translations
    fun getMetadata(): Map<String, Map<String, String>> = translations
}