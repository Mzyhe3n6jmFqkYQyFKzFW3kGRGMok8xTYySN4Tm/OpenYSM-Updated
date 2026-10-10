@file:Suppress("MemberVisibilityCanBePrivate")

package com.elfmcys.yesstevemodel.geckolib3.core.keyframe

import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.BoneKeyFrame
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool

data class BoneAnimation(
    val boneName: String,
    val rotationKeyFrames: MutableList<BoneKeyFrame>,
    val positionKeyFrames: MutableList<BoneKeyFrame>,
    val scaleKeyFrames: MutableList<BoneKeyFrame>
) {
    var boneId: Int = StringPool.computeIfAbsent(boneName)
}