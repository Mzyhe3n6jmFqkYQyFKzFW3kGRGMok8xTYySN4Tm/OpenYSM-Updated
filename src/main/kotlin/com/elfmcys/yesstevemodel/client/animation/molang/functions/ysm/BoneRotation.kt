package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm

import com.elfmcys.yesstevemodel.client.animation.molang.struct.Vec3fStruct
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone

class BoneRotation : BoneParamFunction() {
    override fun getParam(bone: IBone): Vec3fStruct {
        return BoneRotationStruct(bone)
    }

    class BoneRotationStruct(private val boneTransform: IBone) : Vec3fStruct() {
        override fun getX(): Float = -Math.toDegrees(boneTransform.getRotationX().toDouble()).toFloat()
        override fun getY(): Float = -Math.toDegrees(boneTransform.getRotationY().toDouble()).toFloat()
        override fun getZ(): Float = Math.toDegrees(boneTransform.getRotationZ().toDouble()).toFloat()
    }
}