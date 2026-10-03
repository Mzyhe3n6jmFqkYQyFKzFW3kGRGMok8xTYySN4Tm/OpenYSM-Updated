@file:Suppress("unused")

package com.elfmcys.yesstevemodel.geckolib3.core

import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

open class EntityFrameStateTracker<T : Entity>(@JvmField var entity: T) {
    private var currentTick: Int = 0
    private var lastPosition: Vec3? = null

    @JvmField
    var cachedModelId: String? = null

    @JvmField
    var currentTime: Float = 0.0f

    @JvmField
    var timeDelta: Float = 0.0f

    @JvmField
    var positionDelta: Vec3 = Vec3.ZERO
    private val animatedEntities: IntOpenHashSet = IntOpenHashSet()

    open fun reset() {
        animatedEntities.clear()
        currentTick = 0
        lastPosition = null
        positionDelta = Vec3.ZERO
        cachedModelId = null
        currentTime = 0.0f
        timeDelta = 0.0f
    }

    fun updateState(tickCount: Int, seekTime: Float, frameTime: Float) {
        if (currentTick < tickCount) {
            onTickUpdate(tickCount, currentTick)
            currentTick = tickCount
        }
        if (currentTime < seekTime) {
            onTimeUpdate(seekTime, currentTime, frameTime)
            currentTime = seekTime
        }
    }

    open fun setEntity(t: T) {
        entity = t
    }

    open fun onTimeUpdate(currentTick: Float, deltaTick: Float, partialTick: Float) {
        timeDelta = currentTick - deltaTick
        updatePosition(partialTick)
        cachedModelId = null
    }

    open fun onTickUpdate(currentTick: Int, previousTick: Int) {
        animatedEntities.clear()
    }

    open fun updatePosition(f: Float) {
        val vec3 = Vec3(
            Mth.lerp(f.toDouble(), entity.xo, entity.x),
            Mth.lerp(f.toDouble(), entity.yo, entity.y),
            Mth.lerp(f.toDouble(), entity.zo, entity.z)
        )
        val lastPos = lastPosition
        if (lastPos != null) {
            positionDelta = vec3.subtract(lastPos)
        }
        lastPosition = vec3
    }

    open fun markProcessed(i: Int): Boolean = animatedEntities.add(i)

    open fun isProcessed(i: Int): Boolean = animatedEntities.contains(i)

    open fun getPositionDelta(): Vec3 = positionDelta

    open fun getCachedModelId(): String? = cachedModelId

    open fun setCachedModelId(str: String?) {
        cachedModelId = str
    }

    open fun getTimeDelta(): Float = timeDelta
}