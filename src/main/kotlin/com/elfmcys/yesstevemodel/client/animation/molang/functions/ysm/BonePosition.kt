package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm

import com.elfmcys.yesstevemodel.client.animation.molang.struct.Vec3fStruct
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone

class BonePosition : BoneParamFunction() {
    override fun getParam(bone: IBone): Vec3fStruct {
        return BonePositionStruct(bone)
    }

    class BonePositionStruct(private val boneTransform: IBone) : Vec3fStruct() {
        override fun getX(): Float = boneTransform.getPositionX()
        override fun getY(): Float = boneTransform.getPositionY()
        override fun getZ(): Float = boneTransform.getPositionZ()
    }
}