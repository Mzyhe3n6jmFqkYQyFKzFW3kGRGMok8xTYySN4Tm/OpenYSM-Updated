package com.elfmcys.yesstevemodel.geckolib3.core.snapshot

import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone
import org.joml.Vector3f

open class BoneSnapshot(bone: IBone) {
    @JvmField
    var boneId: Int = bone.boneId

    @JvmField
    val position: Vector3f = Vector3f()

    @JvmField
    val rotation: Vector3f = Vector3f()

    @JvmField
    val scale: Vector3f = Vector3f(1.0f, 1.0f, 1.0f)

    @JvmField
    var hidden: Boolean = false

    @JvmField
    var childrenHidden: Boolean = false

    init {
        applyTransform(bone)
    }

    open fun applyTransform(bone: IBone) {
        val initialRotation: Vector3f = bone.initialRotation
        position.set(bone.positionX, bone.positionY, bone.positionZ)
        rotation.set(
            bone.rotationX - initialRotation.x,
            bone.rotationY - initialRotation.y,
            bone.rotationZ - initialRotation.z
        )
        scale.set(bone.scaleX, bone.scaleY, bone.scaleZ)
        hidden = bone.isHidden
        childrenHidden = bone.childBonesAreHiddenToo
    }

    open fun copyFrom(snapshot: BoneSnapshot) {
        position.set(snapshot.position)
        rotation.set(snapshot.rotation)
        scale.set(snapshot.scale)
        hidden = snapshot.hidden
        childrenHidden = snapshot.childrenHidden
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BoneSnapshot) return false
        return boneId == other.boneId
    }

    override fun hashCode(): Int = boneId
}