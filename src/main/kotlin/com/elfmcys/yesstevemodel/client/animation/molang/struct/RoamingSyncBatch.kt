package com.elfmcys.yesstevemodel.client.animation.molang.struct

import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap

class RoamingSyncBatch(
    private val modelHashId: Int,
    private val changedVariables: Int2FloatOpenHashMap
) {
    constructor(modelHashId: Int, initialCapacity: Int) : this(modelHashId, Int2FloatOpenHashMap(initialCapacity))

    fun modelHashId(): Int = modelHashId
    fun changedVariables(): Int2FloatOpenHashMap = changedVariables
}