package com.elfmcys.yesstevemodel.geckolib3.core.keyframe

import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.BoneKeyFrame
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool

open class BoneAnimation(
    @JvmField var boneName: String,
    @JvmField var rotationKeyFrames: MutableList<BoneKeyFrame>,
    @JvmField var positionKeyFrames: MutableList<BoneKeyFrame>,
    @JvmField var scaleKeyFrames: MutableList<BoneKeyFrame>
) {
    @JvmField var boneId: Int = StringPool.computeIfAbsent(boneName)
}