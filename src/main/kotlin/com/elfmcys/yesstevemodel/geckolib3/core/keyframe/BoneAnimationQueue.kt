package com.elfmcys.yesstevemodel.geckolib3.core.keyframe

import com.elfmcys.yesstevemodel.geckolib3.core.controller.PredicateBasedController
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.BoneKeyFrame
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneSnapshot
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneTopLevelSnapshot
import com.elfmcys.yesstevemodel.geckolib3.util.InterpolationLookup
import org.joml.Vector3f

open class BoneAnimationQueue(
    @JvmField val topLevelSnapshot: BoneTopLevelSnapshot
) {
    @JvmField val controllerSnapshot: BoneSnapshot = BoneSnapshot(topLevelSnapshot.bone)
    @JvmField var rotationTimeline: InterpolationLookup<BoneKeyFrame>? = null
    @JvmField var positionTimeline: InterpolationLookup<BoneKeyFrame>? = null
    @JvmField var scaleTimeline: InterpolationLookup<BoneKeyFrame>? = null
    private var animationActive: Boolean = false
    private var blendWeight: Float = 1.0f
    @JvmField var positionOutput: Vector3f? = null
    @JvmField var rotationOutput: Vector3f? = null
    @JvmField var scaleOutput: Vector3f? = null
    @JvmField var overrideMode: Boolean = false
    @JvmField var rotationQueue: AnimationPoint? = null
    @JvmField var positionQueue: AnimationPoint? = null
    @JvmField var scaleQueue: AnimationPoint? = null
    @JvmField val transformProviderRecord: PredicateBasedController.TransformProviderRecord = PredicateBasedController.TransformProviderRecord(this)

    open fun applyAnimation(animation: BoneAnimation, z: Boolean) {
        if (animation.rotationKeyFrames.isNotEmpty()) {
            rotationTimeline = InterpolationLookup(animation.rotationKeyFrames, 0.0f, BoneKeyFrame::getEndTick)
        } else {
            rotationTimeline = null
        }
        if (animation.positionKeyFrames.isNotEmpty()) {
            positionTimeline = InterpolationLookup(animation.positionKeyFrames, 0.0f, BoneKeyFrame::getEndTick)
        } else {
            positionTimeline = null
        }
        if (animation.scaleKeyFrames.isNotEmpty()) {
            scaleTimeline = InterpolationLookup(animation.scaleKeyFrames, 0.0f, BoneKeyFrame::getEndTick)
        } else {
            scaleTimeline = null
        }
        controllerSnapshot.applyTransform(topLevelSnapshot.bone)
        animationActive = true
        overrideMode = z
        resetQueues()
    }

    open fun snapshot(): BoneSnapshot = controllerSnapshot
    open fun rotationQueue(): AnimationPoint? = rotationQueue
    open fun positionQueue(): AnimationPoint? = positionQueue
    open fun scaleQueue(): AnimationPoint? = scaleQueue
    open fun isActive(): Boolean = animationActive

    open fun clear() {
        rotationTimeline = null
        positionTimeline = null
        scaleTimeline = null
        positionOutput = null
        rotationOutput = null
        scaleOutput = null
        animationActive = false
        resetQueues()
    }

    open fun getBlendWeight(): Float = blendWeight

    open fun setBlendWeight(blendWeight: Float) {
        this.blendWeight = if (blendWeight > 0.0f) blendWeight else 0.0f
    }

    open fun resetQueues() {
        rotationQueue = null
        positionQueue = null
        scaleQueue = null
    }
}