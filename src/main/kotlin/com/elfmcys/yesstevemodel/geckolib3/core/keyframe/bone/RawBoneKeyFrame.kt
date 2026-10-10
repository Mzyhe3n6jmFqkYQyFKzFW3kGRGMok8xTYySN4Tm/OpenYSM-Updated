package com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone

import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.FloatValue
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.RotationValue

class RawBoneKeyFrame {
    var startTick: Double = 0.0
    var easingType: EasingType = EasingType.LINEAR
    var preX: Double = 0.0
    var preXValue: IValue? = null
    var preY: Double = 0.0
    var preYValue: IValue? = null
    var preZ: Double = 0.0
    var preZValue: IValue? = null
    var postX: Double = 0.0
    var postXValue: IValue? = null
    var postY: Double = 0.0
    var postYValue: IValue? = null
    var postZ: Double = 0.0
    var postZValue: IValue? = null
    var contiguous: Boolean = true
    var preValue: Vector3v? = null
    var postValue: Vector3v? = null

    private fun getValue(value: IValue?, primitive: Double, isRotation: Boolean, flip: Boolean): IValue {
        if (value == null) {
            if (isRotation) return FloatValue(RotationValue.convert(primitive.toFloat(), flip))
            return FloatValue(primitive.toFloat())
        }
        if (isRotation) return RotationValue(value, flip)
        return value
    }

    fun init(isRotation: Boolean) {
        if (preValue != null) return
        preValue = Vector3v(
            getValue(preXValue, preX, isRotation, true),
            getValue(preYValue, preY, isRotation, true),
            getValue(preZValue, preZ, isRotation, false)
        )
        postValue = if (contiguous) preValue else
            Vector3v(
                getValue(postXValue, postX, isRotation, true),
                getValue(postYValue, postY, isRotation, true),
                getValue(postZValue, postZ, isRotation, false)
            )
    }

    fun startTick(): Float = startTick.toFloat()
    fun easingType(): EasingType = easingType
    fun preValue(): Vector3v = preValue!!
    fun postValue(): Vector3v = postValue!!
}
