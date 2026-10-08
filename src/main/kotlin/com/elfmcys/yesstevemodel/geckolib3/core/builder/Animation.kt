package com.elfmcys.yesstevemodel.geckolib3.core.builder

import com.elfmcys.yesstevemodel.geckolib3.core.event.ParticleEventKeyFrame
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.BoneAnimation
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.event.EventKeyFrame
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import it.unimi.dsi.fastutil.objects.ReferenceArrayList

class Animation(
    var animationName: String,
    animationLength: Double,
    var loop: ILoopType?,
    var unKnowData1: IValue? = null,
    var unKnowData2: IValue? = null,
    var blendWeight: IValue? = null,
    var override: Boolean? = null,
    boneAnimations: Array<BoneAnimation>,
    soundKeyFrames: Array<EventKeyFrame<String>>,
    particleKeyFrames: Array<ParticleEventKeyFrame>,
    customInstructionKeyframes: Array<EventKeyFrame<Array<IValue>>>
) {
    val animationLength: Float = animationLength.toFloat()

    val boneAnimations: MutableList<BoneAnimation> = ReferenceArrayList.wrap(boneAnimations)

    val soundKeyFrames: MutableList<EventKeyFrame<String>> = ReferenceArrayList.wrap(soundKeyFrames)

    val particleKeyFrames: MutableList<ParticleEventKeyFrame> = ReferenceArrayList.wrap(particleKeyFrames)

    val customInstructionKeyframes: MutableList<EventKeyFrame<Array<IValue>>> =
        ReferenceArrayList.wrap(customInstructionKeyframes)

    var isFromPrimaryAssembly: Boolean = false

    var sourceKey: String? = null

    val isEmpty: Boolean
        get() = boneAnimations.isEmpty() && soundKeyFrames.isEmpty() && particleKeyFrames.isEmpty() && customInstructionKeyframes.isEmpty()
}
