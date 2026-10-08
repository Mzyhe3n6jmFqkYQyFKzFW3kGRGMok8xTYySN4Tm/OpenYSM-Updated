package com.elfmcys.yesstevemodel.geckolib3.core.snapshot

import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone
import org.joml.Vector3f

open class BoneTopLevelSnapshot(val bone: IBone) : BoneSnapshot(bone) {
    @JvmField
    val currentValue: Vector3f = Vector3f()

    @JvmField
    var isCurrentlyRunningAnimation: Boolean = false

    @JvmField
    var isCurrentlyRunningRotationAnimation: Boolean = true

    @JvmField
    var isCurrentlyRunningPositionAnimation: Boolean = true

    @JvmField
    var isCurrentlyRunningScaleAnimation: Boolean = true

    @JvmField
    var mostRecentResetRotationTick: Float = 0.0f

    @JvmField
    var mostRecentResetPositionTick: Float = 0.0f

    @JvmField
    var mostRecentResetScaleTick: Float = 0.0f

    @JvmField
    var prevRotation: Vector3f? = null

    @JvmField
    var prevPosition: Vector3f? = null

    @JvmField
    var prevScale: Vector3f? = null

    open fun reset() {
        bone.setHidden(hidden, childrenHidden)
        val initialRotation: Vector3f = bone.initialRotation
        bone.rotationX = rotation.x + initialRotation.x
        bone.rotationY = rotation.y + initialRotation.y
        bone.rotationZ = rotation.z + initialRotation.z
        bone.positionX = position.x
        bone.positionY = position.y
        bone.positionZ = position.z
        bone.scaleX = scale.x
        bone.scaleY = scale.y
        bone.scaleZ = scale.z
        currentValue.set(0.0f, 0.0f, 0.0f)
    }
}