package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm

import com.elfmcys.yesstevemodel.client.animation.molang.struct.Vec3fStruct
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone

class BoneScale : BoneParamFunction() {
    override fun getParam(bone: IBone): Vec3fStruct {
        return BoneScaleStruct(bone)
    }

    class BoneScaleStruct(private val boneTransform: IBone) : Vec3fStruct() {
        override fun getX(): Float = boneTransform.scaleX
        override fun getY(): Float = boneTransform.scaleY
        override fun getZ(): Float = boneTransform.scaleZ
    }
}