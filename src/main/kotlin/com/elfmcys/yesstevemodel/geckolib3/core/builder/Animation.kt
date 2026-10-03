package com.elfmcys.yesstevemodel.geckolib3.core.builder

import com.elfmcys.yesstevemodel.geckolib3.core.event.ParticleEventKeyFrame
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.BoneAnimation
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.event.EventKeyFrame
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import it.unimi.dsi.fastutil.objects.ReferenceArrayList

open class Animation(
    @JvmField var animationName: String,
    animationLength: Double,
    @JvmField var loop: ILoopType?,
    @JvmField var unKnowData1: IValue? = null,
    @JvmField var unKnowData2: IValue? = null,
    @JvmField var blendWeight: IValue? = null,
    @JvmField var override: Boolean? = null,
    boneAnimations: Array<BoneAnimation>,
    soundKeyFrames: Array<EventKeyFrame<String>>,
    particleKeyFrames: Array<ParticleEventKeyFrame>,
    customInstructionKeyframes: Array<EventKeyFrame<Array<IValue>>>
) {
    @JvmField
    var animationLength: Float = animationLength.toFloat()

    @JvmField
    var boneAnimations: MutableList<BoneAnimation> = ReferenceArrayList.wrap(boneAnimations)

    @JvmField
    var soundKeyFrames: MutableList<EventKeyFrame<String>> = ReferenceArrayList.wrap(soundKeyFrames)

    @JvmField
    var particleKeyFrames: MutableList<ParticleEventKeyFrame> = ReferenceArrayList.wrap(particleKeyFrames)

    @JvmField
    var customInstructionKeyframes: MutableList<EventKeyFrame<Array<IValue>>> =
        ReferenceArrayList.wrap(customInstructionKeyframes)

    @JvmField
    var isFromPrimaryAssembly: Boolean = false

    @JvmField
    var sourceKey: String? = null

    open fun isEmpty(): Boolean {
        return boneAnimations.isEmpty() && soundKeyFrames.isEmpty() && particleKeyFrames.isEmpty() && customInstructionKeyframes.isEmpty()
    }
}
