package com.elfmcys.yesstevemodel.geckolib3.core.builder

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap
import it.unimi.dsi.fastutil.ints.Int2ReferenceMaps
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap

open class AnimationController(initialState: String, animationStates: Array<AnimationState>) {
    val stateId: Int = StringPool.computeIfAbsent(initialState)
    val states: Int2ReferenceMap<AnimationState>

    init {
        val map = Int2ReferenceOpenHashMap<AnimationState>()
        for (state in animationStates) {
            map.put(state.hashId, state)
        }
        states = Int2ReferenceMaps.unmodifiable(map)
    }

    open fun getStateId(): Int = stateId
    open fun getStates(): Int2ReferenceMap<AnimationState> = states
}