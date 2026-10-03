package com.elfmcys.yesstevemodel.geckolib3.file

import com.elfmcys.yesstevemodel.audio.AudioTrackData
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue

class ModelExtraResourcesFile(
    val audioTracks: Map<String, AudioTrackData>,
    val functions: Map<String, IValue>,
    val translations: Map<String, Map<String, String>>
)