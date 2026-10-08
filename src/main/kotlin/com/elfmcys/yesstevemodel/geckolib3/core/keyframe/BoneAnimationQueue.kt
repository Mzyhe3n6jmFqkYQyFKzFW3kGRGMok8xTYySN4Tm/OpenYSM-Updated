package com.elfmcys.yesstevemodel.geckolib3.core.keyframe

import com.elfmcys.yesstevemodel.geckolib3.core.controller.PredicateBasedController
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.BoneKeyFrame
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneSnapshot
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneTopLevelSnapshot
import com.elfmcys.yesstevemodel.geckolib3.util.InterpolationLookup
import org.joml.Vector3f

data class BoneAnimationQueue(
    val topLevelSnapshot: BoneTopLevelSnapshot
) {
    private val controllerSnapshot: BoneSnapshot = BoneSnapshot(topLevelSnapshot.bone)
    var rotationTimeline: InterpolationLookup<BoneKeyFrame>? = null
        private set
    var positionTimeline: InterpolationLookup<BoneKeyFrame>? = null
        private set
    var scaleTimeline: InterpolationLookup<BoneKeyFrame>? = null
        private set
    private var animationActive: Boolean = false
    private var blendWeight2: Float = 1.0f
    var positionOutput: Vector3f? = null
    var rotationOutput: Vector3f? = null
    var scaleOutput: Vector3f? = null
    var overrideMode: Boolean = false
        private set
    var rotationQueue: AnimationPoint? = null
    var positionQueue: AnimationPoint? = null
    var scaleQueue: AnimationPoint? = null
    val transformProviderRecord: PredicateBasedController.TransformProviderRecord =
        PredicateBasedController.TransformProviderRecord(this)

    fun applyAnimation(animation: BoneAnimation, z: Boolean) {
        rotationTimeline = if (animation.rotationKeyFrames.isNotEmpty())
            InterpolationLookup(animation.rotationKeyFrames, 0.0f, BoneKeyFrame::endTick) else null
        positionTimeline = if (animation.positionKeyFrames.isNotEmpty())
            InterpolationLookup(animation.positionKeyFrames, 0.0f, BoneKeyFrame::endTick) else null
        scaleTimeline = if (animation.scaleKeyFrames.isNotEmpty())
            InterpolationLookup(animation.scaleKeyFrames, 0.0f, BoneKeyFrame::endTick) else null
        controllerSnapshot.applyTransform(topLevelSnapshot.bone)
        animationActive = true
        overrideMode = z
        resetQueues()
    }

    val snapshot: BoneSnapshot
        get() = controllerSnapshot
    val isActive: Boolean
        get() = animationActive

    fun clear() {
        rotationTimeline = null
        positionTimeline = null
        scaleTimeline = null
        positionOutput = null
        rotationOutput = null
        scaleOutput = null
        animationActive = false
        resetQueues()
    }

    var blendWeight: Float
        get() = blendWeight2
        set(value) {
            blendWeight2 = value.coerceAtLeast(0.0f)
        }

    fun resetQueues() {
        rotationQueue = null
        positionQueue = null
        scaleQueue = null
    }
}