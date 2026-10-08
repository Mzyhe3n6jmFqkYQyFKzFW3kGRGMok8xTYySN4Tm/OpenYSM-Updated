package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm

import com.elfmcys.yesstevemodel.client.animation.molang.struct.Vec3fStruct
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone

class BonePosition : BoneParamFunction() {
    override fun getParam(bone: IBone): Vec3fStruct {
        return BonePositionStruct(bone)
    }

    class BonePositionStruct(private val boneTransform: IBone) : Vec3fStruct() {
        override val x: Float
            get() = boneTransform.positionX
        override val y: Float
            get() = boneTransform.positionY
        override val z: Float
            get() = boneTransform.positionZ
    }
}