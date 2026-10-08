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
    private val geoBoneName: String = geoBone.name
    private val geoBoneBoneId: Int = geoBone.boneId
    private val geoBonePivotX: Float = geoBone.pivotX
    private val geoBonePivotY: Float = geoBone.pivotY
    private val geoBonePivotZ: Float = geoBone.pivotZ
    private val geoBoneInitialRotation: Vector3f = Vector3f(geoBone.rotX, geoBone.rotY, geoBone.rotZ)

    @JvmField
    @PublishedApi
    internal var rawTouhouMaidBone: Any? = null

    override val initialRotation: Vector3f
        get() = geoBoneInitialRotation

    override val pivotAbsX: Float
        get() = stateBuffer[stateOffset + PIVOT_ABS_X_OFFSET]

    override val pivotAbsY: Float
        get() = stateBuffer[stateOffset + PIVOT_ABS_Y_OFFSET]

    override val pivotAbsZ: Float
        get() = stateBuffer[stateOffset + PIVOT_ABS_Z_OFFSET]

    override val name: String
        get() = geoBoneName

    override val boneId: Int
        get() = geoBoneBoneId

    override var rotationX: Float
        get() = matrixData[matrixOffset + ROT_X_OFFSET]
        set(value) {
            matrixData[matrixOffset + ROT_X_OFFSET] = value
        }

    override var rotationY: Float
        get() = matrixData[matrixOffset + ROT_Y_OFFSET]
        set(value) {
            matrixData[matrixOffset + ROT_Y_OFFSET] = value
        }

    override var rotationZ: Float
        get() = matrixData[matrixOffset + ROT_Z_OFFSET]
        set(value) {
            matrixData[matrixOffset + ROT_Z_OFFSET] = value
        }

    override var positionX: Float
        get() = matrixData[matrixOffset + POS_X_OFFSET]
        set(value) {
            matrixData[matrixOffset + POS_X_OFFSET] = value
        }

    override var positionY: Float
        get() = matrixData[matrixOffset + POS_Y_OFFSET]
        set(value) {
            matrixData[matrixOffset + POS_Y_OFFSET] = value
        }

    override var positionZ: Float
        get() = matrixData[matrixOffset + POS_Z_OFFSET]
        set(value) {
            matrixData[matrixOffset + POS_Z_OFFSET] = value
        }

    override var scaleX: Float
        get() = matrixData[matrixOffset + SCALE_X_OFFSET]
        set(value) {
            matrixData[matrixOffset + SCALE_X_OFFSET] = value
        }

    override var scaleY: Float
        get() = matrixData[matrixOffset + SCALE_Y_OFFSET]
        set(value) {
            matrixData[matrixOffset + SCALE_Y_OFFSET] = value
        }

    override var scaleZ: Float
        get() = matrixData[matrixOffset + SCALE_Z_OFFSET]
        set(value) {
            matrixData[matrixOffset + SCALE_Z_OFFSET] = value
        }

    override val pivotX: Float
        get() = geoBonePivotX

    override val pivotY: Float
        get() = geoBonePivotY

    override val pivotZ: Float
        get() = geoBonePivotZ

    override val isHidden: Boolean
        get() = matrixData[matrixOffset + HIDDEN_OFFSET] == 1.0f

    override fun setHidden(hidden: Boolean) {
        setHidden(hidden, hidden)
    }

    override val childBonesAreHiddenToo: Boolean
        get() = matrixData[matrixOffset + HIDE_CHILDREN_OFFSET] == 1.0f

    override fun setHidden(selfHidden: Boolean, skipChildRendering: Boolean) {
        matrixData[matrixOffset + HIDDEN_OFFSET] = if (selfHidden) 1.0f else 0.0f
        matrixData[matrixOffset + HIDE_CHILDREN_OFFSET] = if (skipChildRendering) 1.0f else 0.0f
    }

    override val isTrackingXform: Boolean
        get() = matrixData[matrixOffset + TRACK_XFORM_OFFSET] == 1.0f

    override fun setTrackXform(z: Boolean) {
        matrixData[matrixOffset + TRACK_XFORM_OFFSET] = if (z) 1.0f else 0.0f
    }

    inline fun <reified T> getTouhouMaidBone(): T? {
        if (rawTouhouMaidBone == null) {
            rawTouhouMaidBone = TouhouMaidBoneProcessor.createLocationBone(this)
        }
        return rawTouhouMaidBone as? T
    }

    init {
        setHidden(geoBone.isHidden, geoBone.childBonesAreHiddenToo())
        rotationX = geoBone.rotX
        rotationY = geoBone.rotY
        rotationZ = geoBone.rotZ
        scaleX = 1.0f
        scaleY = 1.0f
        scaleZ = 1.0f
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