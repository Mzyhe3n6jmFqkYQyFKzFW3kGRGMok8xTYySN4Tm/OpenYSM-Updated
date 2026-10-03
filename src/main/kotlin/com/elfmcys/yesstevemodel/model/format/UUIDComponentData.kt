@file:Suppress("unused")

package com.elfmcys.yesstevemodel.model.format

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import net.minecraft.network.chat.Component
import java.util.*

class UUIDComponentData(
    val isEnabled: Boolean,
    val displayComponent: Component?,
    uuidArr: Array<UUID>,
    map: Map<UUID, Component>?
) {
    val uuidSet: Set<UUID> = ImmutableSet.copyOf(uuidArr)
    val uuidComponentMap: Map<UUID, Component>? = if (map == null) null else ImmutableMap.copyOf(map)

    constructor(
        isEnabled: Boolean,
        obj: Any?,
        uuidArr: Array<UUID>,
        map: Map<UUID, Any?>?
    ) : this(
        isEnabled,
        obj as? Component,
        uuidArr,
        map?.mapValues { it.value as Component }
    )
}
