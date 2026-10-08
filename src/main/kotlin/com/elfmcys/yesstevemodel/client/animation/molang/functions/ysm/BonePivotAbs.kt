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
        override val x: Float
            get() = bone.pivotAbsX
        override val y: Float
            get() = bone.pivotAbsY
        override val z: Float
            get() = bone.pivotAbsZ
    }
}