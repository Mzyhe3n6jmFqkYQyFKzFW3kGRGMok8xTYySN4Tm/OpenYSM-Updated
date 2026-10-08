@file:Suppress("unused")

package com.elfmcys.yesstevemodel.geckolib3.core.controller

import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType
import com.elfmcys.yesstevemodel.geckolib3.core.enums.AnimationState
import com.elfmcys.yesstevemodel.geckolib3.core.event.InstructionKeyFrameExecutor
import com.elfmcys.yesstevemodel.geckolib3.core.event.SoundKeyFrameExecutor
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.BoneAnimationQueue
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.ConstantPoint
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.KeyFramePoint
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.TransitionPoint
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.BoneKeyFrame
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.TransitionKeyFrame
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneSnapshot
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneTopLevelSnapshot
import com.elfmcys.yesstevemodel.geckolib3.util.IInterpolable
import com.elfmcys.yesstevemodel.geckolib3.util.InterpolationLookup
import com.elfmcys.yesstevemodel.geckolib3.util.TicksInterpolator
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import com.mojang.datafixers.util.Pair
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import org.joml.Vector3f
import kotlin.math.max

open class AnimationControllerInstance(
    var animatable: AnimatableEntity<*>,
    transitionLengthTicks: Float,
    private var isScaleTransitionSpecial: Boolean = false
) {
    private val boneAnimationQueues: Int2ReferenceOpenHashMap<BoneAnimationQueue> = Int2ReferenceOpenHashMap()
    val activeBoneAnimationQueues: ReferenceArrayList<BoneAnimationQueue> = ReferenceArrayList()
    val context: AnimationControllerContext = AnimationControllerContext()
    private val defaultTransitionTick: Float = 3.0f
    var animationState: AnimationState = AnimationState.IDLE
        private set
    private var tickOffset: Float = 0.0f
    private var transitionInterpolator: IInterpolable = TicksInterpolator(transitionLengthTicks)
    private var savedEndingTick: Float = 0.0f
    private var lastRequestedAnimation: Pair<ILoopType, String>? = null
    private var pendingAnimation: Pair<ILoopType, Animation>? = null
    var currentAnimation: Animation? = null
        private set
    private var currentAnimationLoop: ILoopType? = null
    private var instructionExecutor: InstructionKeyFrameExecutor? = null
    private var soundExecutor: SoundKeyFrameExecutor? = null
    var isAnimationFinished: Boolean = true
        private set

    open fun initBoneQueues(list: MutableList<BoneTopLevelSnapshot>) {
        fullReset()
        for (boneTopLevelSnapshot in list) {
            boneAnimationQueues.put(boneTopLevelSnapshot.boneId, BoneAnimationQueue(boneTopLevelSnapshot))
        }
    }

    open fun setAnimation(animationName: String?) {
        setAnimation(animationName, null)
    }

    open fun setAnimation(animationName: String?, loopType: ILoopType?) {
        if (animationName == null) {
            cancelAnimation()
            return
        }
        val last = lastRequestedAnimation
        if (last != null && last.second == animationName && last.first == loopType) {
            return
        }
        clearAnimation()
        lastRequestedAnimation = Pair(loopType, animationName)
        val animation: Animation = animatable.getAnimation(animationName) ?: return
        pendingAnimation = Pair(loopType ?: animation.loop, animation)
    }

    open fun process(tick: Float, evaluator: ExpressionEvaluator<AnimationContext<*>>, z: Boolean) {
        evaluator.entity().setAnimationControllerContext(context)
        var adjustedTick: Float = adjustTick(tick)
        if (animationState == AnimationState.ENDING_TRANSITION && adjustedTick >= defaultTransitionTick) {
            clearAnimation()
        }
        val currAnim = currentAnimation
        if (animationState == AnimationState.RUNNING && currAnim != null && currentAnimationLoop == ILoopType.EDefaultLoopTypes.PLAY_ONCE && adjustedTick >= currAnim.animationLength) {
            executeRemainingEvents(evaluator, z)
            startEndingTransition(tick)
            context.executeRenderLayers(evaluator)
            adjustedTick = adjustTick(tick)
        }
        if (animationState == AnimationState.IDLE) {
            context.executeRenderLayers(evaluator)
            if (!applyPendingAnimation()) return
            tickOffset = tick
            adjustedTick = 0.0f
            animationState = if (transitionInterpolator.progress > 0.0f) {
                AnimationState.BEGINNING_TRANSITION
            } else {
                AnimationState.RUNNING
            }
        }
        resetAllQueues()
        if (animationState == AnimationState.BEGINNING_TRANSITION) {
            if (adjustedTick < transitionInterpolator.progress) {
                context.animTime = 0.0f
                processBeginningTransition(evaluator, adjustedTick)
                return
            } else {
                adjustedTick -= transitionInterpolator.progress
                tickOffset = tick - adjustedTick
                animationState = AnimationState.RUNNING
            }
        }
        val runningAnim = currentAnimation
        if (animationState == AnimationState.RUNNING && runningAnim != null) {
            if (adjustedTick > runningAnim.animationLength) {
                isAnimationFinished = true
                when (currentAnimationLoop) {
                    ILoopType.EDefaultLoopTypes.LOOP -> {
                        context.executeRenderLayers(evaluator)
                        adjustedTick = if (runningAnim.animationLength > 0.0f) {
                            adjustedTick % runningAnim.animationLength
                        } else {
                            0.0f
                        }
                        executeRemainingEvents(evaluator, z)
                        tickOffset = tick - adjustedTick
                    }

                    ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME -> {
                        adjustedTick = runningAnim.animationLength
                    }
                }
            }
            context.animTime = adjustedTick / 20.0f
            executeTimelineEvents(evaluator, adjustedTick, z)
            processRunningAnimation(evaluator, adjustedTick)
            return
        }
        if (animationState == AnimationState.ENDING_TRANSITION) {
            if (adjustedTick > defaultTransitionTick) {
                adjustedTick = defaultTransitionTick
            }
            context.animTime = savedEndingTick / 20.0f
            processEndingTransition(evaluator, adjustedTick)
        }
    }

    open fun getContext(): AnimationControllerContext = context

    open fun executeRemainingEvents(animationTick: ExpressionEvaluator<AnimationContext<*>>, z: Boolean) {
        val anim = currentAnimation ?: return
        context.animTime = anim.animationLength / 20.0f
        instructionExecutor?.let {
            it.executeRemaining(animationTick, z)
            it.reset()
        }
        soundExecutor?.reset()
    }

    open fun executeRenderLayers(evaluator: ExpressionEvaluator<AnimationContext<*>>) {
        evaluator.entity().setAnimationControllerContext(context)
        context.executeRenderLayers(evaluator)
        evaluator.entity().setAnimationControllerContext(null)
    }

    open fun executeTimelineEvents(
        evaluator: ExpressionEvaluator<AnimationContext<*>>,
        currentTick: Float,
        isActive: Boolean
    ) {
        soundExecutor?.playSound(animatable, currentTick, isActive)
        instructionExecutor?.executeTo(evaluator, currentTick, isActive)
    }

    open fun startEndingTransition(tick: Float) {
        if (animationState == AnimationState.RUNNING || animationState == AnimationState.BEGINNING_TRANSITION) {
            var adjustedTick: Float = adjustTick(tick)
            for (animationQueue in activeBoneAnimationQueues) {
                val rotVal = animationQueue.rotationQueue?.cachedValue
                if (rotVal != null) {
                    animationQueue.rotationOutput = Vector3f(rotVal)
                }
                val posVal = animationQueue.positionQueue?.cachedValue
                if (posVal != null) {
                    animationQueue.positionOutput = Vector3f(posVal)
                }
                val scaleVal = animationQueue.scaleQueue?.cachedValue
                if (scaleVal != null) {
                    animationQueue.scaleOutput = Vector3f(scaleVal)
                }
            }
            tickOffset = tick
            val anim = currentAnimation
            if (animationState == AnimationState.RUNNING && anim != null) {
                if (adjustedTick > anim.animationLength) {
                    adjustedTick = anim.animationLength
                }
                savedEndingTick = adjustedTick
            } else {
                savedEndingTick = 0.0f
            }
            isAnimationFinished = true
            animationState = AnimationState.ENDING_TRANSITION
        }
    }

    open fun processBeginningTransition(evaluator: ExpressionEvaluator<AnimationContext<*>>, tick: Float) {
        val anim = currentAnimation ?: return
        val blendWeight: Float = anim.blendWeight?.evalAsFloat(evaluator) ?: 1.0f
        val lerpFactor: Float = transitionInterpolator.interpolate(tick)
        for (boneAnimationQueue in activeBoneAnimationQueues) {
            boneAnimationQueue.setBlendWeight(blendWeight)
            val boneSnapshot: BoneSnapshot = boneAnimationQueue.snapshot()
            val rotTimeline = boneAnimationQueue.rotationTimeline
            if (rotTimeline != null) {
                boneAnimationQueue.rotationQueue =
                    getTransitionPointAtTick(rotTimeline, tick, lerpFactor, boneSnapshot.rotation)
            }
            val posTimeline = boneAnimationQueue.positionTimeline
            if (posTimeline != null) {
                boneAnimationQueue.positionQueue =
                    getTransitionPointAtTick(posTimeline, tick, lerpFactor, boneSnapshot.position)
            }
            val scaleTimeline = boneAnimationQueue.scaleTimeline
            if (scaleTimeline != null) {
                val transitionPoint = if (isScaleTransitionSpecial) {
                    getTransitionPointAtTick(
                        scaleTimeline,
                        transitionInterpolator.progress,
                        1.0f,
                        boneSnapshot.scale
                    )
                } else {
                    getTransitionPointAtTick(scaleTimeline, tick, lerpFactor, boneSnapshot.scale)
                }
                boneAnimationQueue.scaleQueue = transitionPoint
            }
        }
    }

    open fun processRunningAnimation(evaluator: ExpressionEvaluator<AnimationContext<*>>, tick: Float) {
        val anim = currentAnimation ?: return
        val blendWeight: Float = anim.blendWeight?.evalAsFloat(evaluator) ?: 1.0f
        for (boneAnimationQueue in activeBoneAnimationQueues) {
            boneAnimationQueue.setBlendWeight(blendWeight)
            val rotTimeline = boneAnimationQueue.rotationTimeline
            if (rotTimeline != null) {
                boneAnimationQueue.rotationQueue = getKeyFramePointAtTick(rotTimeline, tick)
            }
            val posTimeline = boneAnimationQueue.positionTimeline
            if (posTimeline != null) {
                boneAnimationQueue.positionQueue = getKeyFramePointAtTick(posTimeline, tick)
            }
            val scaleTimeline = boneAnimationQueue.scaleTimeline
            if (scaleTimeline != null) {
                boneAnimationQueue.scaleQueue = getKeyFramePointAtTick(scaleTimeline, tick)
            }
        }
    }

    open fun processEndingTransition(evaluator: ExpressionEvaluator<AnimationContext<*>>, f: Float) {
        val anim = currentAnimation ?: return
        val blendWeight: Float = anim.blendWeight?.evalAsFloat(evaluator) ?: 1.0f
        for (boneAnimationQueue in activeBoneAnimationQueues) {
            boneAnimationQueue.setBlendWeight(blendWeight)
            val posOut = boneAnimationQueue.positionOutput
            if (posOut != null) {
                boneAnimationQueue.positionQueue = getConstantPointAtTick(f, posOut, boneAnimationQueue.overrideMode)
            }
            val rotOut = boneAnimationQueue.rotationOutput
            if (rotOut != null) {
                boneAnimationQueue.rotationQueue = getConstantPointAtTick(f, rotOut, boneAnimationQueue.overrideMode)
            }
            val scaleOut = boneAnimationQueue.scaleOutput
            if (scaleOut != null) {
                boneAnimationQueue.scaleQueue = getConstantPointAtTick(f, scaleOut, boneAnimationQueue.overrideMode)
            }
        }
    }

    open fun resetAllQueues() {
        if (animationState != AnimationState.IDLE) {
            for (activeBoneAnimationQueue in activeBoneAnimationQueues) {
                activeBoneAnimationQueue.resetQueues()
            }
        }
    }

    open fun getKeyFramePointAtTick(frames: InterpolationLookup<BoneKeyFrame>, tick: Float): KeyFramePoint {
        val frame: BoneKeyFrame = frames.getAtTime(tick)
        return KeyFramePoint(tick - frame.getStartTick(), frame, context)
    }

    open fun getTransitionPointAtTick(
        frames: InterpolationLookup<BoneKeyFrame>,
        tick: Float,
        lerpFactor: Float,
        offsetPoint: Vector3f
    ): TransitionPoint {
        return TransitionPoint(
            tick,
            lerpFactor,
            transitionInterpolator.progress,
            offsetPoint,
            frames.getAtTime(0.0f) as TransitionKeyFrame,
            context
        )
    }

    open fun getConstantPointAtTick(tick: Float, offsetPoint: Vector3f, isInstant: Boolean): ConstantPoint {
        return ConstantPoint(tick, if (isInstant) 0.0f else defaultTransitionTick, offsetPoint, context)
    }

    open fun applyPendingAnimation(): Boolean {
        val pair: Pair<ILoopType, Animation> = pendingAnimation ?: return false
        pendingAnimation = null
        val anim = pair.second
        currentAnimation = anim
        currentAnimationLoop = pair.first
        isAnimationFinished = false
        for (animation in anim.boneAnimations) {
            val queue: BoneAnimationQueue? = boneAnimationQueues.get(animation.boneId)
            if (queue != null) {
                queue.applyAnimation(animation, animation.scaleKeyFrames.isNotEmpty())
                activeBoneAnimationQueues.add(queue)
            }
        }
        instructionExecutor = InstructionKeyFrameExecutor(anim.customInstructionKeyframes)
        soundExecutor = SoundKeyFrameExecutor(anim.soundKeyFrames, context.audioPlayerManager)
        return true
    }

    open fun clearAnimation() {
        if (animationState != AnimationState.IDLE) {
            animationState = AnimationState.IDLE
            soundExecutor?.reset()
            soundExecutor = null
            instructionExecutor = null
            for (activeBoneAnimationQueue in activeBoneAnimationQueues) {
                activeBoneAnimationQueue.clear()
            }
            activeBoneAnimationQueues.clear()
            currentAnimation = null
            isAnimationFinished = true
        }
    }

    open fun getCurrentAnimation(): Animation? = currentAnimation
    open fun getAnimationState(): AnimationState = animationState
    open fun getActiveBoneAnimationQueues(): ReferenceArrayList<BoneAnimationQueue> = activeBoneAnimationQueues
    open fun isAnimationFinished(): Boolean = isAnimationFinished
    open fun setTransitionInterpolator(interpolable: IInterpolable) {
        transitionInterpolator = interpolable
    }

    open fun getInterpolated(): Float = transitionInterpolator.progress * 20.0f
    open fun adjustTick(tick: Float): Float = max(tick - tickOffset, 0.0f)
    open fun stopSound() {
        soundExecutor?.stop()
    }

    open fun cancelAnimation() {
        lastRequestedAnimation = null
        pendingAnimation = null
        clearAnimation()
    }

    open fun fullReset() {
        cancelAnimation()
        boneAnimationQueues.clear()
    }

    open fun resetRequestedAnimation() {
        lastRequestedAnimation = null
    }

    open fun beginEndingTransition(tick: Float) {
        startEndingTransition(tick)
    }
}