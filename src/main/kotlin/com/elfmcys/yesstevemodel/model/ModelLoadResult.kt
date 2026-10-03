@file:Suppress("unused")

package com.elfmcys.yesstevemodel.model

import com.elfmcys.yesstevemodel.model.format.ServerModelData
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps
import it.unimi.dsi.fastutil.objects.ObjectSets
import net.minecraft.network.chat.Component

class ModelLoadResult(
    val isSuccess: Boolean,
    val errorMessage: Component?,
    map: Map<String, ServerModelData>?,
    strArr: Array<String>?
) {
    val modelDefinitions: Map<String, ServerModelData> =
        if (map == null) Object2ReferenceMaps.emptyMap() else ImmutableMap.copyOf(map)
    val authModelIds: Set<String> =
        if (strArr == null) ObjectSets.emptySet() else ImmutableSet.copyOf(strArr)

    constructor(
        isSuccess: Boolean,
        errorMessage: Any?,
        map: Map<String, ServerModelData>?,
        strArr: Array<String>?
    ) : this(
        isSuccess,
        errorMessage as? Component,
        map,
        strArr
    )
}
