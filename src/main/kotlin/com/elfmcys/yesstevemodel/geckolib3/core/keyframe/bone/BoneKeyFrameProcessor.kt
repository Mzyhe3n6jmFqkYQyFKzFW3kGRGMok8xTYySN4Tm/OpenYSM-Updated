package com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone

import it.unimi.dsi.fastutil.objects.ReferenceArrayList

object BoneKeyFrameProcessor {
    @JvmStatic
    fun process(frames: Array<RawBoneKeyFrame>, isRotation: Boolean): MutableList<BoneKeyFrame> {
        return process(ReferenceArrayList.wrap(frames), isRotation)
    }

    @JvmStatic
    fun process(frames: MutableList<RawBoneKeyFrame>, isRotation: Boolean): MutableList<BoneKeyFrame> {
        for (frame in frames) {
            frame.init(isRotation)
        }
        val list = arrayOfNulls<BoneKeyFrame>(frames.size)
        for (i in 0 until frames.size) {
            val end: RawBoneKeyFrame = frames[i]
            val easingType: EasingType = if (end.easingType() == EasingType.CATMULLROM || i == 0) {
                end.easingType()
            } else {
                frames[i - 1].easingType()
            }
            list[i] = easingType.buildKeyFrame(frames, i)
        }
        @Suppress("UNCHECKED_CAST")
        return ReferenceArrayList.wrap(list as Array<BoneKeyFrame>)
    }
}