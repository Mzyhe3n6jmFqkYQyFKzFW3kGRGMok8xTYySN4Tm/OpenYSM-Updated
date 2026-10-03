package com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone

import kotlin.math.max
import kotlin.math.min

// Native Access：所有字段都有讀取
fun interface EasingType {

    fun buildKeyFrame(keyFrames: MutableList<RawBoneKeyFrame>, index: Int): BoneKeyFrame

    companion object {
        @JvmStatic
        fun buildTransitionKeyFrame(keyFrames: MutableList<RawBoneKeyFrame>): TransitionKeyFrame {
            val transitionDst = keyFrames[0]
            return TransitionKeyFrame(transitionDst.startTick(), transitionDst.preValue(), transitionDst.postValue())
        }

        @JvmStatic
        fun buildLinearKeyFrame(keyFrames: MutableList<RawBoneKeyFrame>, index: Int): BoneKeyFrame {
            if (index == 0) {
                return buildTransitionKeyFrame(keyFrames)
            }
            val begin = keyFrames[index - 1]
            val end = keyFrames[index]
            return LinearKeyFrame(
                begin.startTick(),
                end.startTick() - begin.startTick(),
                begin.postValue(),
                end.preValue(),
                end.postValue()
            )
        }

        @JvmStatic
        fun buildCatmullRomKeyFrame(keyFrames: MutableList<RawBoneKeyFrame>, index: Int): BoneKeyFrame {
            if (index == 0) {
                return buildTransitionKeyFrame(keyFrames)
            }
            val left = keyFrames[max(0, index - 2)]
            val begin = keyFrames[index - 1]
            val end = keyFrames[index]
            val right = keyFrames[min(keyFrames.size - 1, index + 1)]
            return CatmullRomKeyFrame(
                begin.startTick(),
                end.startTick() - begin.startTick(),
                left.postValue(),
                begin.postValue(),
                end.preValue(),
                right.preValue(),
                end.postValue()
            )
        }

        @JvmField
        val LINEAR: EasingType = EasingType { keyFrames, index -> buildLinearKeyFrame(keyFrames, index) }

        @JvmField
        val CATMULLROM: EasingType = EasingType { keyFrames, index -> buildCatmullRomKeyFrame(keyFrames, index) }
    }
}
