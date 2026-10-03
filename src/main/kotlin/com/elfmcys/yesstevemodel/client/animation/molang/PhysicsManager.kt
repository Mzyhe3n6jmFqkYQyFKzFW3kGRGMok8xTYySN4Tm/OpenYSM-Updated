package com.elfmcys.yesstevemodel.client.animation.molang

import com.elfmcys.yesstevemodel.client.animation.molang.functions.physics.IPhysics
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap

class PhysicsManager {
    val physicsValues: Int2ReferenceOpenHashMap<IPhysics> = Int2ReferenceOpenHashMap(16)
    var lastRenderTicks: Float = 0.0f

    fun update(renderTicks: Float) {
        if (lastRenderTicks > 0) {
            if (renderTicks > lastRenderTicks) {
                val interval: Float = (renderTicks - lastRenderTicks) / 20.0f
                lastRenderTicks = renderTicks
                physicsValues.int2ReferenceEntrySet().fastForEach { entry -> entry.value.update(interval) }
            }
        } else {
            lastRenderTicks = renderTicks
        }
    }

    fun put(key: Int, physics: IPhysics) {
        physicsValues.put(key, physics)
    }

    fun get(key: Int): IPhysics? {
        return physicsValues.get(key)
    }

    fun clear() {
        lastRenderTicks = 0.0f
        physicsValues.clear()
    }
}