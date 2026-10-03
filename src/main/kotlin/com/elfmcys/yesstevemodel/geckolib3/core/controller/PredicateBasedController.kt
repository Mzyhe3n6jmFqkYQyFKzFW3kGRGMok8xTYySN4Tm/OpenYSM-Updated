package com.elfmcys.yesstevemodel.geckolib3.core.controller

import com.elfmcys.yesstevemodel.audio.PlaybackFlags
import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType
import com.elfmcys.yesstevemodel.geckolib3.core.enums.AnimationState
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
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
import com.elfmcys.yesstevemodel.geckolib3.util.IInterpolable
import com.elfmcys.yesstevemodel.geckolib3.util.TicksInterpolator
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap
import org.joml.Vector3f
import java.util.*
import java.util.function.Consumer

@Suppress("UNCHECKED_CAST")
open class PredicateBasedController<T : AnimatableEntity<*>>(
    private val animatable: T,
    private val name: String,
    private val transitionLengthTicks: Float,
    predicate: IAnimationPredicate<*>,
    private val deprecatedMode: Boolean = false
) : IAnimationController<T> {
    private val predicate: IAnimationPredicate<T> = predicate as IAnimationPredicate<T>
    private val transitionInterpolator: AnimationControllerInstance =
        AnimationControllerInstance(animatable, transitionLengthTicks, true)
    private val playbackFlags: PlaybackFlags = PlaybackFlags(false)
    private var soundIValue: IValue? = null
    private var needsReset: Boolean = false

    constructor(animatable: T, name: String, transitionLengthTicks: Float, predicate: IAnimationPredicate<*>) : this(
        animatable,
        name,
        transitionLengthTicks,
        predicate,
        false
    )

    override fun process(
        event: AnimationEvent<T>,
        evaluator: ExpressionEvaluator<AnimationContext<*>>,
        isMoving: Boolean
    ) {
        event.setController(this)
        var playState: PlayState? = handleSoundExpression(evaluator)
        if (playState == null) {
            playState = predicate.predicate(event, evaluator)
        }
        event.setController(null)
        when (playState) {
            PlayState.CONTINUE -> {
                transitionInterpolator.process(event.currentTick, evaluator, isMoving)
                needsReset = false
            }

            PlayState.STOP -> {
                val state: AnimationState = transitionInterpolator.animationState
                if (state == AnimationState.BEGINNING_TRANSITION || state == AnimationState.RUNNING) {
                    transitionInterpolator.beginEndingTransition(event.currentTick)
                    transitionInterpolator.resetRequestedAnimation()
                }
                if (state == AnimationState.ENDING_TRANSITION) {
                    transitionInterpolator.process(event.currentTick, evaluator, isMoving)
                }
                needsReset = false
            }

            PlayState.PAUSE -> {
                transitionInterpolator.process(event.currentTick, evaluator, false)
                transitionInterpolator.resetAllQueues()
                needsReset = true
            }
        }
        event.getAnimatable().setAnimationState(name, transitionInterpolator.animationState)
    }

    private fun handleSoundExpression(expressionEvaluator: ExpressionEvaluator<AnimationContext<*>>): PlayState? {
        val soundExpr = soundIValue ?: return null
        playbackFlags.setStopped(transitionInterpolator.isAnimationFinished)
        playbackFlags.setPaused(transitionInterpolator.isAnimationFinished)
        expressionEvaluator.entity().setPlaybackFlags(playbackFlags)
        expressionEvaluator.entity().setAnimationControllerContext(transitionInterpolator.context)
        expressionEvaluator.entity().setIsClientSide(true)
        val state: Int = soundExpr.evalAsInt(expressionEvaluator)
        expressionEvaluator.entity().setIsClientSide(false)
        expressionEvaluator.entity().setAnimationControllerContext(null)
        expressionEvaluator.entity().setPlaybackFlags(null)
        return when (state) {
            2 -> PlayState.CONTINUE
            3 -> PlayState.STOP
            4 -> PlayState.PAUSE
            else -> null
        }
    }

    override fun init(
        list: MutableList<BoneTopLevelSnapshot>,
        object2ReferenceMap: Object2ReferenceMap<String, MutableList<IValue>>
    ) {
        transitionInterpolator.initBoneQueues(list)
        transitionInterpolator.setTransitionInterpolator(TicksInterpolator(transitionLengthTicks))
        soundIValue = null
        val list2: MutableList<IValue>? = object2ReferenceMap.get(name.replace(".", "_ctrl_"))
        if (!list2.isNullOrEmpty()) {
            soundIValue = list2[0]
        }
    }

    override fun getName(): String = name

    override fun getCurrentAnimation(): String {
        if (transitionInterpolator.animationState == AnimationState.IDLE) {
            return "Coded"
        }
        return "Coded -> " + (transitionInterpolator.currentAnimation?.animationName ?: "")
    }

    open fun setAnimation(animationName: String?) {
        transitionInterpolator.setAnimation(animationName, null)
    }

    open fun setAnimation(animation: String?, loopType: ILoopType?) {
        transitionInterpolator.setAnimation(animation, loopType)
    }

    open fun setTransitionLengthTicks(ticks: Float) {
        if (transitionInterpolator.getInterpolated() != ticks) {
            transitionInterpolator.setTransitionInterpolator(TicksInterpolator(ticks))
        }
    }

    open fun setInterpolator(interpolator: IInterpolable) {
        transitionInterpolator.setTransitionInterpolator(interpolator)
    }

    open fun evaluateExpressions(evaluator: ExpressionEvaluator<AnimationContext<*>>) {
        transitionInterpolator.executeRenderLayers(evaluator)
    }

    override fun forEachTransform(consumer: Consumer<BoneTransformProvider>) {
        if (!needsReset) {
            val queues = transitionInterpolator.activeBoneAnimationQueues
            val size = queues.size
            for (i in 0 until size) {
                consumer.accept(queues.get(i).transformProviderRecord)
            }
        }
    }

    open fun clearAnimation() {
        transitionInterpolator.cancelAnimation()
    }

    override fun reset() {
        transitionInterpolator.fullReset()
        soundIValue = null
    }

    open fun stopTransition() {
        transitionInterpolator.resetRequestedAnimation()
    }

    open fun beginEndTransition(currentTick: Float) {
        transitionInterpolator.beginEndingTransition(currentTick)
    }

    open fun isPlaying(): Boolean = transitionInterpolator.isAnimationFinished

    open fun markDirty() {
        transitionInterpolator.stopSound()
    }

    @Deprecated("")
    override fun isDeprecatedMode(): Boolean {
        return deprecatedMode && transitionInterpolator.animationState == AnimationState.RUNNING
    }

    class TransformProviderRecord(private val data: BoneAnimationQueue) : BoneTransformProvider {
        private val mutableVector: TransitionVector3f = TransitionVector3f(0f, 0f, 0f)
        private val rotScratch: EulerNlerpScratch = EulerNlerpScratch()

        override fun getBoneTarget(): BoneTopLevelSnapshot = data.topLevelSnapshot

        override fun getRotation(evaluator: ExpressionEvaluator<AnimationContext<*>>): TransitionVector3f? {
            val point: AnimationPoint = data.rotationQueue ?: return null
            if (point is ConstantPoint) {
                mutableVector.set(point.getLerpPoint(evaluator))
                mutableVector.setPercentCompleted(point.getPercentCompleted())
                val blendWeight: Float = data.getBlendWeight()
                if (blendWeight != 1.0f) {
                    mutableVector.mul(blendWeight)
                }
            } else if (point is TransitionPoint) {
                val vector3fMul: Vector3f = point.evaluateRaw(evaluator).mul(data.getBlendWeight())
                MathUtil.nlerpEulerAngles(
                    point.getLerpFactor(),
                    point.getOffsetPoint(),
                    vector3fMul,
                    data.topLevelSnapshot.bone.getInitialRotation(),
                    vector3fMul,
                    rotScratch
                )
                mutableVector.set(vector3fMul)
                mutableVector.setPercentCompleted(0.0f)
            } else {
                mutableVector.set(point.getLerpPoint(evaluator))
                val blendWeight2: Float = data.getBlendWeight()
                if (blendWeight2 != 1.0f) {
                    mutableVector.mul(blendWeight2)
                }
                mutableVector.setPercentCompleted(0.0f)
            }
            return mutableVector
        }

        override fun getPosition(evaluator: ExpressionEvaluator<AnimationContext<*>>): TransitionVector3f? {
            val point: AnimationPoint = data.positionQueue ?: return null
            mutableVector.set(point.getLerpPoint(evaluator))
            var blendWeight: Float = data.getBlendWeight()
            if (point is ConstantPoint) {
                mutableVector.setPercentCompleted(point.getPercentCompleted())
            } else {
                if (point is TransitionPoint) {
                    blendWeight = MathUtil.lerpValues(point.getLerpFactor(), 1.0f, blendWeight)
                }
                mutableVector.setPercentCompleted(0.0f)
            }
            if (blendWeight != 1.0f) {
                mutableVector.mul(blendWeight)
            }
            return mutableVector
        }

        override fun getScale(evaluator: ExpressionEvaluator<AnimationContext<*>>): TransitionVector3f? {
            val point: AnimationPoint = data.scaleQueue ?: return null
            mutableVector.set(point.getLerpPoint(evaluator))
            var blendWeight: Float = data.getBlendWeight()
            if (point is ConstantPoint) {
                mutableVector.setPercentCompleted(point.getPercentCompleted())
            } else {
                if (point is TransitionPoint) {
                    blendWeight = MathUtil.lerpValues(point.getLerpFactor(), 1.0f, blendWeight)
                }
                mutableVector.setPercentCompleted(0.0f)
            }
            if (blendWeight != 1.0f) {
                MathUtil.lerpAnglesInPlace(mutableVector, blendWeight, mutableVector)
            }
            return mutableVector
        }

        fun data(): BoneAnimationQueue = data

        override fun equals(other: Any?): Boolean {
            if (other === this) return true
            if (other == null || other.javaClass != this.javaClass) return false
            val that = other as TransformProviderRecord
            return Objects.equals(data, that.data)
        }

        override fun hashCode(): Int = Objects.hash(data)

        override fun toString(): String = "TransformProviderRecord[data=$data]"
    }
}