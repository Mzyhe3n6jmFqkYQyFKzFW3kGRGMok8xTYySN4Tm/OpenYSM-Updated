package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm

import com.elfmcys.yesstevemodel.client.animation.molang.struct.Vec3fStruct
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone

class BoneRotation : BoneParamFunction() {
    override fun getParam(bone: IBone): Vec3fStruct {
        return BoneRotationStruct(bone)
    }

    class BoneRotationStruct(private val boneTransform: IBone) : Vec3fStruct() {
        override val x: Float
            get() = -Math.toDegrees(boneTransform.rotationX.toDouble()).toFloat()
        override val y: Float
            get() = -Math.toDegrees(boneTransform.rotationY.toDouble()).toFloat()
        override val z: Float
            get() = Math.toDegrees(boneTransform.rotationZ.toDouble()).toFloat()
    }
}