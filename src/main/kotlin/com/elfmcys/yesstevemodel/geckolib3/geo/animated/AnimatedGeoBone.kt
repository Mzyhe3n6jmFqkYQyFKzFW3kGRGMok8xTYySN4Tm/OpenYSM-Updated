package com.elfmcys.yesstevemodel.geckolib3.geo.animated

import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoBone
import org.joml.Vector3f
import rip.ysm.compat.touhoulittlemaid.TouhouMaidBoneProcessor

class AnimatedGeoBone(
    geoBone: GeoBone,
    private val matrixData: FloatArray,
    private val matrixOffset: Int,
    private val stateBuffer: FloatArray,
    private val stateOffset: Int
) : IBone {
    private val name: String = geoBone.name
    private val boneId: Int = geoBone.boneId
    private val pivotX: Float = geoBone.pivotX
    private val pivotY: Float = geoBone.pivotY
    private val pivotZ: Float = geoBone.pivotZ
    private val initialRotation: Vector3f = Vector3f(geoBone.rotX, geoBone.rotY, geoBone.rotZ)
    private var touhouMaidBone: Any? = null

    init {
        setHidden(geoBone.isHidden, geoBone.childBonesAreHiddenToo())
        setRotationX(geoBone.rotX)
        setRotationY(geoBone.rotY)
        setRotationZ(geoBone.rotZ)
        setScaleX(1.0f)
        setScaleY(1.0f)
        setScaleZ(1.0f)
    }

    override fun getInitialRotation(): Vector3f = initialRotation

    override fun getPivotAbsX(): Float = stateBuffer[stateOffset + PIVOT_ABS_X_OFFSET]

    override fun getPivotAbsY(): Float = stateBuffer[stateOffset + PIVOT_ABS_Y_OFFSET]

    override fun getPivotAbsZ(): Float = stateBuffer[stateOffset + PIVOT_ABS_Z_OFFSET]

    override fun getName(): String = name

    override fun getBoneId(): Int = boneId

    override fun getRotationX(): Float = matrixData[matrixOffset + ROT_X_OFFSET]

    override fun setRotationX(value: Float) {
        matrixData[matrixOffset + ROT_X_OFFSET] = value
    }

    override fun getRotationY(): Float = matrixData[matrixOffset + ROT_Y_OFFSET]

    override fun setRotationY(value: Float) {
        matrixData[matrixOffset + ROT_Y_OFFSET] = value
    }

    override fun getRotationZ(): Float = matrixData[matrixOffset + ROT_Z_OFFSET]

    override fun setRotationZ(value: Float) {
        matrixData[matrixOffset + ROT_Z_OFFSET] = value
    }

    override fun getPositionX(): Float = matrixData[matrixOffset + POS_X_OFFSET]

    override fun setPositionX(value: Float) {
        matrixData[matrixOffset + POS_X_OFFSET] = value
    }

    override fun getPositionY(): Float = matrixData[matrixOffset + POS_Y_OFFSET]

    override fun setPositionY(value: Float) {
        matrixData[matrixOffset + POS_Y_OFFSET] = value
    }

    override fun getPositionZ(): Float = matrixData[matrixOffset + POS_Z_OFFSET]

    override fun setPositionZ(value: Float) {
        matrixData[matrixOffset + POS_Z_OFFSET] = value
    }

    override fun getScaleX(): Float = matrixData[matrixOffset + SCALE_X_OFFSET]

    override fun setScaleX(value: Float) {
        matrixData[matrixOffset + SCALE_X_OFFSET] = value
    }

    override fun getScaleY(): Float = matrixData[matrixOffset + SCALE_Y_OFFSET]

    override fun setScaleY(value: Float) {
        matrixData[matrixOffset + SCALE_Y_OFFSET] = value
    }

    override fun getScaleZ(): Float = matrixData[matrixOffset + SCALE_Z_OFFSET]

    override fun setScaleZ(value: Float) {
        matrixData[matrixOffset + SCALE_Z_OFFSET] = value
    }

    override fun getPivotX(): Float = pivotX

    override fun getPivotY(): Float = pivotY

    override fun getPivotZ(): Float = pivotZ

    override fun isHidden(): Boolean = matrixData[matrixOffset + HIDDEN_OFFSET] == 1.0f

    override fun setHidden(hidden: Boolean) {
        setHidden(hidden, hidden)
    }

    override fun childBonesAreHiddenToo(): Boolean = matrixData[matrixOffset + HIDE_CHILDREN_OFFSET] == 1.0f

    override fun setHidden(selfHidden: Boolean, skipChildRendering: Boolean) {
        matrixData[matrixOffset + HIDDEN_OFFSET] = if (selfHidden) 1.0f else 0.0f
        matrixData[matrixOffset + HIDE_CHILDREN_OFFSET] = if (skipChildRendering) 1.0f else 0.0f
    }

    override fun isTrackingXform(): Boolean = matrixData[matrixOffset + TRACK_XFORM_OFFSET] == 1.0f

    override fun setTrackXform(z: Boolean) {
        matrixData[matrixOffset + TRACK_XFORM_OFFSET] = if (z) 1.0f else 0.0f
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getTouhouMaidBone(): T? {
        if (touhouMaidBone == null) {
            touhouMaidBone = TouhouMaidBoneProcessor.createLocationBone(this)
        }
        return touhouMaidBone as? T
    }

    companion object {
        const val ROT_X_OFFSET: Int = 0
        const val ROT_Y_OFFSET: Int = 1
        const val ROT_Z_OFFSET: Int = 2
        const val POS_X_OFFSET: Int = 3
        const val POS_Y_OFFSET: Int = 4
        const val POS_Z_OFFSET: Int = 5
        const val SCALE_X_OFFSET: Int = 6
        const val SCALE_Y_OFFSET: Int = 7
        const val SCALE_Z_OFFSET: Int = 8
        const val HIDDEN_OFFSET: Int = 9
        const val HIDE_CHILDREN_OFFSET: Int = 10
        const val TRACK_XFORM_OFFSET: Int = 11
        const val PIVOT_ABS_X_OFFSET: Int = 0
        const val PIVOT_ABS_Y_OFFSET: Int = 1
        const val PIVOT_ABS_Z_OFFSET: Int = 2
    }
}