package com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone

import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.FloatValue
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.RotationValue

open class RawBoneKeyFrame {
    @JvmField var startTick: Double = 0.0
    @JvmField var easingType: EasingType = EasingType.LINEAR
    @JvmField var preX: Double = 0.0
    @JvmField var preXValue: IValue? = null
    @JvmField var preY: Double = 0.0
    @JvmField var preYValue: IValue? = null
    @JvmField var preZ: Double = 0.0
    @JvmField var preZValue: IValue? = null
    @JvmField var postX: Double = 0.0
    @JvmField var postXValue: IValue? = null
    @JvmField var postY: Double = 0.0
    @JvmField var postYValue: IValue? = null
    @JvmField var postZ: Double = 0.0
    @JvmField var postZValue: IValue? = null
    @JvmField var contiguous: Boolean = true
    @JvmField var preValue: Vector3v? = null
    @JvmField var postValue: Vector3v? = null

    private fun getValue(value: IValue?, primitive: Double, isRotation: Boolean, flip: Boolean): IValue {
        if (value == null) {
            if (isRotation) {
                return FloatValue(RotationValue.convert(primitive.toFloat(), flip))
            }
            return FloatValue(primitive.toFloat())
        }
        if (isRotation) {
            return RotationValue(value, flip)
        }
        return value
    }

    open fun init(isRotation: Boolean) {
        if (preValue != null) {
            return
        }
        preValue = Vector3v(
            getValue(preXValue, preX, isRotation, true),
            getValue(preYValue, preY, isRotation, true),
            getValue(preZValue, preZ, isRotation, false)
        )
        if (contiguous) {
            postValue = preValue
        } else {
            postValue = Vector3v(
                getValue(postXValue, postX, isRotation, true),
                getValue(postYValue, postY, isRotation, true),
                getValue(postZValue, postZ, isRotation, false)
            )
        }
    }

    open fun startTick(): Float = startTick.toFloat()
    open fun easingType(): EasingType = easingType
    open fun preValue(): Vector3v = preValue!!
    open fun postValue(): Vector3v = postValue!!
}
