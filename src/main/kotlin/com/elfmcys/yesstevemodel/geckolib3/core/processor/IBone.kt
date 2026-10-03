package com.elfmcys.yesstevemodel.geckolib3.core.processor

import org.joml.Vector3f

interface IBone {
    fun getRotationX(): Float
    fun setRotationX(value: Float)
    fun getRotationY(): Float
    fun setRotationY(value: Float)
    fun getRotationZ(): Float
    fun setRotationZ(value: Float)
    fun getPositionX(): Float
    fun setPositionX(value: Float)
    fun getPositionY(): Float
    fun setPositionY(value: Float)
    fun getPositionZ(): Float
    fun setPositionZ(value: Float)
    fun getScaleX(): Float
    fun setScaleX(value: Float)
    fun getScaleY(): Float
    fun setScaleY(value: Float)
    fun getScaleZ(): Float
    fun setScaleZ(value: Float)
    fun getPivotX(): Float
    fun getPivotY(): Float
    fun getPivotZ(): Float
    fun isHidden(): Boolean
    fun setHidden(hidden: Boolean)
    fun childBonesAreHiddenToo(): Boolean
    fun setHidden(selfHidden: Boolean, skipChildRendering: Boolean)
    fun isTrackingXform(): Boolean
    fun setTrackXform(z: Boolean)
    fun getInitialRotation(): Vector3f
    fun getPivotAbsX(): Float
    fun getPivotAbsY(): Float
    fun getPivotAbsZ(): Float
    fun getName(): String
    fun getBoneId(): Int
}