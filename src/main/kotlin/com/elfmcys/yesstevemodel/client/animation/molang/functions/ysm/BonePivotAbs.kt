package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm

import com.elfmcys.yesstevemodel.client.animation.molang.struct.Vec3fStruct
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone

class BonePivotAbs : BoneParamFunction() {
    override fun getParam(bone: IBone): Vec3fStruct {
        if (!bone.isTrackingXform) {
            bone.setTrackXform(true)
        }
        return BonePivotAbsStruct(bone)
    }

    class BonePivotAbsStruct(private val bone: IBone) : Vec3fStruct() {
        override fun getX(): Float = bone.pivotAbsX
        override fun getY(): Float = bone.pivotAbsY
        override fun getZ(): Float = bone.pivotAbsZ
    }
}