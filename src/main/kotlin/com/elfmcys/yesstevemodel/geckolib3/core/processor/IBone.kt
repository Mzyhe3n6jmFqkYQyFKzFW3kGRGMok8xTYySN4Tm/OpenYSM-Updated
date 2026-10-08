package com.elfmcys.yesstevemodel.geckolib3.core.processor

import org.joml.Vector3f

interface IBone {
    var rotationX: Float
    var rotationY: Float
    var rotationZ: Float
    var positionX: Float
    var positionY: Float
    var positionZ: Float
    var scaleX: Float
    var scaleY: Float
    var scaleZ: Float
    val pivotX: Float
    val pivotY: Float
    val pivotZ: Float
    val isHidden: Boolean
    fun setHidden(hidden: Boolean)
    val childBonesAreHiddenToo: Boolean
    fun setHidden(selfHidden: Boolean, skipChildRendering: Boolean)
    val isTrackingXform: Boolean
    fun setTrackXform(z: Boolean)
    val initialRotation: Vector3f
    val pivotAbsX: Float
    val pivotAbsY: Float
    val pivotAbsZ: Float
    val name: String
    val boneId: Int
}