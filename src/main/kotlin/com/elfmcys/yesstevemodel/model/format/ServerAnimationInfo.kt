package com.elfmcys.yesstevemodel.model.format

import it.unimi.dsi.fastutil.objects.*

class ServerAnimationInfo(
    animations: Map<String, Array<String>>,
    textures: Array<String>
) {
    val animations: Map<String, Set<String>> = Object2ObjectMaps.unmodifiable(
        Object2ObjectOpenHashMap(
            animations.entries.associate { (key, value) ->
                key to ObjectSets.unmodifiable(ObjectOpenHashSet.of(*value))
            }
        )
    )

    val textures: List<String> = ObjectLists.unmodifiable(ObjectArrayList.of(*textures))
}
