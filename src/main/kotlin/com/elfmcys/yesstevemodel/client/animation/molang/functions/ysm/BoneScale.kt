package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm

import com.elfmcys.yesstevemodel.client.animation.molang.struct.Vec3fStruct
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone

class BoneScale : BoneParamFunction() {
    override fun getParam(bone: IBone): Vec3fStruct {
        return BoneScaleStruct(bone)
    }

    class BoneScaleStruct(private val boneTransform: IBone) : Vec3fStruct() {
        override fun getX(): Float = boneTransform.getScaleX()
        override fun getY(): Float = boneTransform.getScaleY()
        override fun getZ(): Float = boneTransform.getScaleZ()
    }
}