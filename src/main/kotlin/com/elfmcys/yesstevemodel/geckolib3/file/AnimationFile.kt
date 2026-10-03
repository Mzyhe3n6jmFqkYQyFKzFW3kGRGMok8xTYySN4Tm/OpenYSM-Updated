package com.elfmcys.yesstevemodel.geckolib3.file

import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation
import it.unimi.dsi.fastutil.objects.Object2ReferenceLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps

class AnimationFile(map: Map<String, Animation>) {
    val animations: Map<String, Animation> =
        Object2ReferenceMaps.unmodifiable(Object2ReferenceLinkedOpenHashMap(map))
}