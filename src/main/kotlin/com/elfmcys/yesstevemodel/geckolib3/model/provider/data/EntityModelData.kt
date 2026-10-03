package com.elfmcys.yesstevemodel.geckolib3.model.provider.data

open class EntityModelData {
    @JvmField
    var isSitting: Boolean = false

    @JvmField
    var isChild: Boolean = false

    @JvmField
    var netHeadYaw: Float = 0.0f

    @JvmField
    var rawNetHeadYaw: Float = 0.0f

    @JvmField
    var headPitch: Float = 0.0f

    @JvmField
    var rawHeadPitch: Float = 0.0f

    @JvmField
    var lerpedAge: Float = 0.0f

    @JvmField
    var lerpBodyRot: Float = 0.0f
}