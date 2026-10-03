package com.elfmcys.yesstevemodel.geckolib3.file

import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap

class AnimationControllerFile(animationControllers: Map<String, AnimationController>) {
    val animationControllers: Map<String, AnimationController> =
        Object2ReferenceMaps.unmodifiable(Object2ReferenceOpenHashMap(animationControllers))
}