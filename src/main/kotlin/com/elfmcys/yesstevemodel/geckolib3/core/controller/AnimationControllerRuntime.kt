package com.elfmcys.yesstevemodel.geckolib3.core.controller

import com.elfmcys.yesstevemodel.audio.PlaybackFlags
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.AnimationPoint
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.BoneAnimationQueue
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.ConstantPoint
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.TransitionPoint
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneTopLevelSnapshot
import com.elfmcys.yesstevemodel.geckolib3.core.util.EulerNlerpScratch
import com.elfmcys.yesstevemodel.geckolib3.core.util.MathUtil
import com.elfmcys.yesstevemodel.geckolib3.core.util.TransitionVector3f
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import it.unimi.dsi.fastutil.Pair
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import it.unimi.dsi.fastutil.objects.ReferenceLists
import org.apache.commons.lang3.StringUtils
import org.joml.Vector3f

@Suppress("unused")
class AnimationControllerRuntime<T : AnimatableEntity<*>>(
    private val animatable: T,
    override val name: String,
    private val transitionLengthTicks: Float
) : IAnimationController<T> {
    private var boneTargets: MutableList<BoneTopLevelSnapshot>? = null
    private var animationEntries: AnimationController? = null
    private var currentEntry2: AnimationState? = null
    private var displayName: String? = null
    private var childController: AnimationControllerRuntime<T>? = null
    private val animationSlots: ReferenceArrayList<AnimationSlot> = ReferenceArrayList(8)
    private var activeSlotCount: Int = 0
    private val boneTransformMap: Int2ReferenceOpenHashMap<BoneBlendState> = Int2ReferenceOpenHashMap(64)
    private val activeBoneTransforms: ReferenceArrayList<BoneBlendState> = ReferenceArrayList(16)
    private var needsRebuild: Boolean = false
    private val visitedEntries: IntOpenHashSet = IntOpenHashSet(4)
    private var parentName: String? = null
    private var depth: Int = 1
    private val playbackFlags: PlaybackFlags = PlaybackFlags(true)

    override fun process(
        event: AnimationEvent<T>,
        evaluator: ExpressionEvaluator<AnimationContext<*>>,
        isSomething: Boolean
    ) {
        if (animationEntries == null) return
        evaluator.entity().setAnimationControllerContext(null)
        evaluator.entity().setPlaybackFlags(playbackFlags)
        val currentTick = event.currentTick
        visitedEntries.clear()
        var transitioned = false
        while (evaluateTransitions(evaluator)) {
            transitioned = true
            if (activeSlotCount != 0) break
        }
        val currEntry = currentEntry2
        if (currEntry?.subName != null && depth != MAX_DEPTH) {
            if (transitioned) {
                val subControllerName =
                    if (depth > 1) "${parentName}.${currEntry.subName}" else currEntry.subName
                val childCtrl = animatable.getAnimationEntries("${name}.${subControllerName}")
                if (childCtrl != null) {
                    val existingChild = childController
                    if (existingChild == null) {
                        val newChild = AnimationControllerRuntime(animatable, name, transitionLengthTicks)
                        newChild.initWithBones(boneTargets ?: ReferenceLists.emptyList(), childCtrl)
                        childController = newChild
                    } else {
                        existingChild.updateAnimationEntries(childCtrl)
                    }
                    childController?.setParentInfo(subControllerName, depth + 1)
                }
            }
            childController?.process(event, evaluator, isSomething)
            return
        }
        for (slotIndex in 0 until activeSlotCount) {
            val slot = animationSlots[slotIndex]
            slot.condition.evaluate(evaluator)
            slot.resampler.process(currentTick, evaluator, isSomething && slot.condition.isActive())
        }
        if (needsRebuild) {
            for (i2 in 0 until activeSlotCount) {
                val slot2 = animationSlots[i2]
                for (boneQueue in slot2.animationControllerInstance.activeBoneAnimationQueues) {
                    val blendState = boneTransformMap.get(boneQueue.topLevelSnapshot.boneId)
                    if (!blendState.checkMarked()) {
                        blendState.mark()
                        activeBoneTransforms.add(blendState)
                    }
                    blendState.addBlendSource(slot2.condition, boneQueue)
                }
            }
            needsRebuild = false
        }
    }

    override val currentAnimation: String
        get() {
            val currEntry = currentEntry2
            if (currEntry != null) {
                if (currEntry.subName != null && childController != null)
                    return childController?.currentAnimation ?: "(null)"
                return displayName ?: "(null)"
            }
            return "(null)"
        }

    private fun updateDisplayName(stateName: String) {
        displayName = if (depth > 1) "[$parentName] $stateName" else stateName
    }

    val currentEntry: AnimationState?
        get() = currentEntry2

    val isBuiltinAnimation: Boolean
        get() = currentEntry2?.isBuiltinEntry == true

    override fun init(
        list: MutableList<BoneTopLevelSnapshot>,
        object2ReferenceMap: Object2ReferenceMap<String, MutableList<IValue>>
    ) {
        val childCtrl = animatable.getAnimationEntries(name)
        if (childCtrl != null) {
            initWithBones(list, childCtrl)
        } else {
            reset()
        }
    }

    fun initWithBones(list: MutableList<BoneTopLevelSnapshot>, controller: AnimationController) {
        reset()
        animationEntries = controller
        for (snapshot in list) {
            boneTransformMap.put(snapshot.boneId, BoneBlendState(snapshot))
        }
        boneTargets = list
    }

    private fun updateAnimationEntries(controller: AnimationController) {
        animationEntries = controller
    }

    private fun setParentInfo(parentName: String?, depth: Int) {
        this.parentName = parentName
        this.depth = depth
    }

    private fun evaluateTransitions(evaluator: ExpressionEvaluator<AnimationContext<*>>): Boolean {
        val entries = animationEntries ?: return false
        val currEntry = currentEntry2
        if (currEntry == null) {
            val nextState = entries.states.get(entries.stateId) ?: return false
            visitedEntries.add(nextState.hashId)
            updateDisplayName(nextState.name)
            transitionToEntry(nextState, evaluator)
            return true
        }
        var activeCount = 0
        playbackFlags.setStopped(false)
        playbackFlags.setPaused(true)
        for (slotIndex in 0 until activeSlotCount) {
            val slot = animationSlots[slotIndex]
            if (slot.condition.isActive()) {
                activeCount++
                if (slot.animationControllerInstance.isAnimationFinished) {
                    playbackFlags.setStopped(true)
                } else {
                    playbackFlags.setPaused(false)
                }
            }
        }
        if (activeCount == 0) {
            playbackFlags.setStopped(true)
        }
        for (transition in currEntry.transitions) {
            if (transition.right().evalAsBoolean(evaluator)) {
                val nextState2 = entries.states.get(transition.leftInt())
                if (nextState2 == null || !visitedEntries.add(nextState2.hashId)) {
                    return false
                }
                updateDisplayName(nextState2.name)
                transitionToEntry(nextState2, evaluator)
                return true
            }
        }
        return false
    }

    private fun transitionToEntry(nextState: AnimationState?, evaluator: ExpressionEvaluator<AnimationContext<*>>) {
        val curr = currentEntry2
        if (nextState == null && curr == null) return
        playbackFlags.audioPlayerManager?.stopAll()
        evaluator.entity().setIsClientSide(true)
        if (curr != null) {
            if (curr.subName != null && childController != null) {
                childController?.transitionToEntry(null, evaluator)
            }
            for (value in curr.postExpressions) {
                value.evalSafe(evaluator)
            }
        }
        if (nextState != null) {
            for (value in nextState.preExpressions) {
                value.evalSafe(evaluator)
            }
            for (str in nextState.soundEffects) {
                if (StringUtils.isNotBlank(str)) {
                    playbackFlags.audioPlayerManager
                        ?.playSound(evaluator.entity().geoInstance(), 0, str, false, null)
                }
            }
        }
        evaluator.entity().setIsClientSide(false)
        currentEntry2 = nextState
        for (activeBoneTransform in activeBoneTransforms) {
            activeBoneTransform.resetAndClear()
        }
        activeBoneTransforms.clear()
        needsRebuild = true
        val size =
            if (nextState == null || nextState.isBuiltinEntry || nextState.subName != null) 0 else nextState.animations.size
        for (size2 in animationSlots.size until size) {
            animationSlots.add(AnimationSlot(animatable, transitionLengthTicks))
        }
        for (i in size until activeSlotCount) {
            val animInstance = animationSlots[i].animationControllerInstance
            animInstance.executeRenderLayers(evaluator)
            animInstance.cancelAnimation()
        }
        activeSlotCount = size
        if (nextState != null) {
            for (i in 0 until size) {
                val slot = animationSlots[i]
                val pair = nextState.animations[i]
                if (slot.isNewSlot) {
                    slot.resampler.initBoneQueues(boneTargets ?: ReferenceLists.emptyList())
                    slot.markInitialized()
                }
                slot.condition.setExpression(pair.value)
                slot.resampler.executeRenderLayers(evaluator)
                slot.resampler.setTransitionInterpolator(nextState.blendTransition.asInterpolator())
                slot.resampler.resetRequestedAnimation()
                slot.resampler.setAnimation(pair.key)
            }
        }
    }

    override fun forEachTransform(consumer: (BoneTransformProvider) -> Unit) {
        val curr = currentEntry2
        if (curr != null) {
            if (curr.subName != null && childController != null) {
                childController?.forEachTransform(consumer)
                return
            }
            val list = activeBoneTransforms
            val size = list.size
            for (i in 0 until size) {
                val blendState = list[i]
                if (blendState.hasActiveSources()) {
                    consumer(blendState)
                }
            }
        }
    }

    override fun reset() {
        boneTargets = ReferenceLists.emptyList()
        animationEntries = null
        currentEntry2 = null
        activeSlotCount = 0
        activeBoneTransforms.clear()
        boneTransformMap.clear()
        if (depth == 1) {
            childController = null
        }
        for (animationSlot in animationSlots) {
            animationSlot.animationControllerInstance.fullReset()
        }
        animationSlots.clear()
        playbackFlags.audioPlayerManager?.stopAll()
    }

    private class AnimationSlot(entity: AnimatableEntity<*>, f: Float) {
        val animationControllerInstance: AnimationControllerInstance = AnimationControllerInstance(entity, f)
        val condition: ConditionalEvaluator = ConditionalEvaluator()
        var isNewSlot: Boolean = true
            private set

        val resampler: AnimationControllerInstance get() = animationControllerInstance

        fun markNew() {
            isNewSlot = true
        }

        fun markInitialized() {
            isNewSlot = false
        }
    }

    private class ConditionalEvaluator {
        private var expression: IValue? = null
        private var active: Boolean = true

        fun setExpression(value: IValue?) {
            expression = value
            if (value == null) {
                active = true
            }
        }

        fun evaluate(evaluator: ExpressionEvaluator<*>) {
            val expr = expression
            if (expr != null) {
                active = expr.evalAsBoolean(evaluator)
            }
        }

        fun isActive(): Boolean = active
    }

    private class BoneBlendState(override val boneTarget: BoneTopLevelSnapshot) : BoneTransformProvider {
        private val blendSources: ReferenceArrayList<Pair<ConditionalEvaluator, BoneAnimationQueue>> =
            ReferenceArrayList(4)
        private var isMarked: Boolean = false
        private val rotationOut: TransitionVector3f = TransitionVector3f(0f, 0f, 0f)
        private val positionOut: TransitionVector3f = TransitionVector3f(0f, 0f, 0f)
        private val scaleOut: TransitionVector3f = TransitionVector3f(1.0f, 1.0f, 1.0f)
        private val scaleLerpTmp: Vector3f = Vector3f()
        private val rotScratch: EulerNlerpScratch = EulerNlerpScratch()

        val boneId: Int
            get() = boneTarget.boneId

        fun addBlendSource(evaluator: ConditionalEvaluator, queue: BoneAnimationQueue) {
            blendSources.add(Pair.of(evaluator, queue))
        }

        fun hasActiveSources(): Boolean {
            val size = blendSources.size
            for (i in 0 until size) if (blendSources[i].left().isActive()) return true
            return false
        }

        fun checkMarked(): Boolean = isMarked

        fun mark() {
            isMarked = true
        }

        fun resetAndClear() {
            isMarked = false
            blendSources.clear()
        }

        override fun getRotation(evaluator: ExpressionEvaluator<AnimationContext<*>>): TransitionVector3f? {
            val sources = blendSources
            val size = sources.size
            if (size == 0) return null
            var animationPoint: AnimationPoint?
            val transitionVector3f = rotationOut
            transitionVector3f.set(0.0f, 0.0f, 0.0f)
            transitionVector3f.percentCompleted = 1.0f
            var hasData = false
            var isFirst = true
            var isTransition = false
            var offsetPoint: Vector3f? = null
            var initialRotation: Vector3f? = null
            var lerpFactor = 0.0f
            for (i in 0 until size) {
                val pair = sources[i]
                if (pair.left().isActive()) {
                    val boneQueue = pair.right()
                    val point = boneQueue.rotationQueue
                    if (boneQueue.isActive() && point != null) {
                        animationPoint = point
                        hasData = true
                        if (isFirst) {
                            isFirst = false
                            if (animationPoint is TransitionPoint) {
                                isTransition = true
                                offsetPoint = animationPoint.getOffsetPoint()
                                lerpFactor = animationPoint.getLerpFactor()
                                transitionVector3f.setPercentCompleted(0.0f)
                                initialRotation = boneQueue.topLevelSnapshot.bone.initialRotation
                            }
                        }
                        if (!isTransition) {
                            val lerpPoint = animationPoint.getLerpPoint(evaluator)
                            var blendWeight = boneQueue.getBlendWeight()
                            if (animationPoint is ConstantPoint) {
                                val percentCompleted = animationPoint.getPercentCompleted()
                                blendWeight *= 1.0f - percentCompleted
                                transitionVector3f.setPercentCompleted(percentCompleted)
                            } else {
                                transitionVector3f.setPercentCompleted(0.0f)
                            }
                            transitionVector3f.fma(blendWeight, lerpPoint)
                        } else {
                            if (lerpFactor <= -1.0E-5f || lerpFactor >= 1.0E-5f) {
                                transitionVector3f.fma(
                                    boneQueue.getBlendWeight(),
                                    (animationPoint as TransitionPoint).evaluateRaw(evaluator)
                                )
                            } else {
                                offsetPoint?.let { transitionVector3f.set(it) }
                                return transitionVector3f
                            }
                        }
                    }
                }
            }
            if (hasData) {
                if (isTransition && offsetPoint != null) {
                    MathUtil.nlerpEulerAngles(
                        lerpFactor,
                        offsetPoint,
                        transitionVector3f,
                        initialRotation ?: Vector3f(),
                        transitionVector3f,
                        rotScratch
                    )
                }
                return transitionVector3f
            }
            return null
        }

        override fun getPosition(evaluator: ExpressionEvaluator<AnimationContext<*>>): TransitionVector3f? {
            val sources = blendSources
            val size = sources.size
            if (size == 0) return null
            var point: AnimationPoint?
            val result = positionOut
            result.set(0.0f, 0.0f, 0.0f)
            result.percentCompleted = 1.0f
            var hasData = false
            var isFirst = true
            var isTransition = false
            var offsetPoint: Vector3f? = null
            var lerpFactor = 0.0f
            for (i in 0 until size) {
                val pair = sources[i]
                if (pair.left().isActive()) {
                    val boneQueue = pair.right()
                    val p = boneQueue.positionQueue
                    if (boneQueue.isActive() && p != null) {
                        point = p
                        hasData = true
                        if (isFirst) {
                            isFirst = false
                            if (point is TransitionPoint) {
                                isTransition = true
                                offsetPoint = point.getOffsetPoint()
                                lerpFactor = point.getLerpFactor()
                                result.setPercentCompleted(0.0f)
                            }
                        }
                        if (!isTransition) {
                            val lerpPoint = point.getLerpPoint(evaluator)
                            var blendWeight = boneQueue.getBlendWeight()
                            if (point is ConstantPoint) {
                                val percentCompleted = point.getPercentCompleted()
                                blendWeight *= 1.0f - percentCompleted
                                result.setPercentCompleted(percentCompleted)
                            } else {
                                result.setPercentCompleted(0.0f)
                            }
                            result.fma(blendWeight, lerpPoint)
                        } else {
                            if (lerpFactor <= -1.0E-5f || lerpFactor >= 1.0E-5f) {
                                result.fma(
                                    boneQueue.getBlendWeight(),
                                    (point as TransitionPoint).evaluateRaw(evaluator)
                                )
                            } else {
                                offsetPoint?.let { result.set(it) }
                                return result
                            }
                        }
                    }
                }
            }
            if (hasData) {
                if (isTransition && offsetPoint != null) {
                    MathUtil.lerpValues(lerpFactor, offsetPoint, result, result)
                }
                return result
            }
            return null
        }

        override fun getScale(evaluator: ExpressionEvaluator<AnimationContext<*>>): TransitionVector3f? {
            val sources = blendSources
            val size = sources.size
            if (size == 0) return null
            var point: AnimationPoint?
            val result = scaleOut
            result.set(1.0f, 1.0f, 1.0f)
            result.percentCompleted = 1.0f
            val tmp = scaleLerpTmp
            var hasData = false
            var isFirst = true
            var isTransition = false
            var offsetPoint: Vector3f? = null
            var lerpFactor = 0.0f
            for (i in 0 until size) {
                val pair = sources[i]
                if (pair.left().isActive()) {
                    val boneQueue = pair.right()
                    val p = boneQueue.scaleQueue
                    if (boneQueue.isActive() && p != null) {
                        point = p
                        hasData = true
                        if (isFirst) {
                            isFirst = false
                            if (point is TransitionPoint) {
                                isTransition = true
                                offsetPoint = point.getOffsetPoint()
                                lerpFactor = point.getLerpFactor()
                                result.setPercentCompleted(0.0f)
                            }
                        }
                        if (!isTransition) {
                            val lerpPoint = point.getLerpPoint(evaluator)
                            var blendWeight = boneQueue.getBlendWeight()
                            if (point is ConstantPoint) {
                                val percentCompleted = point.getPercentCompleted()
                                blendWeight *= 1.0f - percentCompleted
                                result.setPercentCompleted(percentCompleted)
                            } else {
                                result.setPercentCompleted(0.0f)
                            }
                            if (blendWeight == 1.0f) {
                                result.mul(lerpPoint)
                            } else {
                                MathUtil.lerpAnglesInPlace(lerpPoint, blendWeight, tmp)
                                result.mul(tmp)
                            }
                        } else {
                            if (lerpFactor <= -1.0E-5f || lerpFactor >= 1.0E-5f) {
                                MathUtil.lerpAnglesInPlace(
                                    (point as TransitionPoint).evaluateRaw(evaluator),
                                    boneQueue.getBlendWeight(),
                                    tmp
                                )
                                result.mul(tmp)
                            } else {
                                offsetPoint?.let { result.set(it) }
                                return result
                            }
                        }
                    }
                }
            }
            if (hasData) {
                if (isTransition && offsetPoint != null) {
                    MathUtil.lerpValues(lerpFactor, offsetPoint, result, result)
                }
                return result
            }
            return null
        }
    }

    companion object {
        const val MAX_DEPTH: Int = 5
    }
}