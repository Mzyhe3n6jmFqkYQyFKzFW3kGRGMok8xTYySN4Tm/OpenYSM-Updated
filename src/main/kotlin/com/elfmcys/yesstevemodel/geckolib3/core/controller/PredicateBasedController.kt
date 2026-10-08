package com.elfmcys.yesstevemodel.geckolib3.core.controller

import com.elfmcys.yesstevemodel.audio.PlaybackFlags
import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType
import com.elfmcys.yesstevemodel.geckolib3.core.enums.AnimationState
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
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
import java.util.*

@Suppress("unused")
class PredicateBasedController<T : AnimatableEntity<*>>(
    animatable: T,
    override val name: String,
    private val transitionLengthTicks: Float,
    predicate: IAnimationPredicate<*>,
    private val deprecatedMode: Boolean = false
) : IAnimationController<T> {
    @Suppress("UNCHECKED_CAST")
    private val predicate: IAnimationPredicate<T> = predicate as IAnimationPredicate<T>
    private val transitionInterpolator: AnimationControllerInstance =
        AnimationControllerInstance(animatable, transitionLengthTicks, true)
    private val playbackFlags: PlaybackFlags = PlaybackFlags(false)
    private var soundIValue: IValue? = null
    private var needsReset: Boolean = false

    override fun process(
        event: AnimationEvent<T>,
        evaluator: ExpressionEvaluator<AnimationContext<*>>,
        isSomething: Boolean
    ) {
        event.controller = this
        var playState = handleSoundExpression(evaluator)
        if (playState == null) {
            playState = predicate.predicate(event, evaluator)
        }
        event.controller = null
        when (playState) {
            PlayState.CONTINUE -> {
                transitionInterpolator.process(event.currentTick, evaluator, isSomething)
                needsReset = false
            }

            PlayState.STOP -> {
                val state = transitionInterpolator.animationState
                if (state == AnimationState.BEGINNING_TRANSITION || state == AnimationState.RUNNING) {
                    transitionInterpolator.beginEndingTransition(event.currentTick)
                    transitionInterpolator.resetRequestedAnimation()
                }
                if (state == AnimationState.ENDING_TRANSITION) {
                    transitionInterpolator.process(event.currentTick, evaluator, isSomething)
                }
                needsReset = false
            }

            PlayState.PAUSE -> {
                transitionInterpolator.process(event.currentTick, evaluator, false)
                transitionInterpolator.resetAllQueues()
                needsReset = true
            }
        }
        event.animatable.setAnimationState(name, transitionInterpolator.animationState)
    }

    private fun handleSoundExpression(expressionEvaluator: ExpressionEvaluator<AnimationContext<*>>): PlayState? {
        val soundExpr = soundIValue ?: return null
        playbackFlags.setStopped(transitionInterpolator.isAnimationFinished)
        playbackFlags.setPaused(transitionInterpolator.isAnimationFinished)
        expressionEvaluator.entity().setPlaybackFlags(playbackFlags)
        expressionEvaluator.entity().setAnimationControllerContext(transitionInterpolator.context)
        expressionEvaluator.entity().setIsClientSide(true)
        val state = soundExpr.evalAsInt(expressionEvaluator)
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
        val list2 = object2ReferenceMap[name.replace(".", "_ctrl_")]
        if (!list2.isNullOrEmpty()) {
            soundIValue = list2[0]
        }
    }

    override val currentAnimation: String
        get() {
            if (transitionInterpolator.animationState == AnimationState.IDLE) {
                return "Coded"
            }
            return "Coded -> " + (transitionInterpolator.currentAnimation?.animationName ?: "")
        }

    fun setAnimation(animationName: String?) {
        transitionInterpolator.setAnimation(animationName, null)
    }

    fun setAnimation(animation: String?, loopType: ILoopType?) {
        transitionInterpolator.setAnimation(animation, loopType)
    }

    fun setTransitionLengthTicks(ticks: Float) {
        if (transitionInterpolator.interpolated != ticks) {
            transitionInterpolator.setTransitionInterpolator(TicksInterpolator(ticks))
        }
    }

    fun setInterpolator(interpolator: IInterpolable) {
        transitionInterpolator.setTransitionInterpolator(interpolator)
    }

    fun evaluateExpressions(evaluator: ExpressionEvaluator<AnimationContext<*>>) {
        transitionInterpolator.executeRenderLayers(evaluator)
    }

    override fun forEachTransform(consumer: (BoneTransformProvider) -> Unit) {
        if (!needsReset) {
            val queues = transitionInterpolator.activeBoneAnimationQueues
            val size = queues.size
            for (i in 0 until size) {
                consumer(queues[i].transformProviderRecord)
            }
        }
    }

    fun clearAnimation() {
        transitionInterpolator.cancelAnimation()
    }

    override fun reset() {
        transitionInterpolator.fullReset()
        soundIValue = null
    }

    fun stopTransition() {
        transitionInterpolator.resetRequestedAnimation()
    }

    fun beginEndTransition(currentTick: Float) {
        transitionInterpolator.beginEndingTransition(currentTick)
    }

    fun isPlaying(): Boolean = transitionInterpolator.isAnimationFinished

    fun markDirty() {
        transitionInterpolator.stopSound()
    }

    override val isDeprecatedMode: Boolean
        get() = deprecatedMode && transitionInterpolator.animationState == AnimationState.RUNNING

    class TransformProviderRecord(private val data: BoneAnimationQueue) : BoneTransformProvider {
        private val mutableVector: TransitionVector3f = TransitionVector3f(0f, 0f, 0f)
        private val rotScratch = EulerNlerpScratch()

        override val boneTarget: BoneTopLevelSnapshot
            get() = data.topLevelSnapshot

        override fun getRotation(evaluator: ExpressionEvaluator<AnimationContext<*>>): TransitionVector3f? {
            val point = data.rotationQueue ?: return null
            when (point) {
                is ConstantPoint -> {
                    mutableVector.set(point.getLerpPoint(evaluator))
                    mutableVector.setPercentCompleted(point.percentCompleted)
                    val blendWeight = data.blendWeight
                    if (blendWeight != 1.0f) {
                        mutableVector.mul(blendWeight)
                    }
                }

                is TransitionPoint -> {
                    val vector3fMul = point.evaluateRaw(evaluator).mul(data.blendWeight)
                    MathUtil.nlerpEulerAngles(
                        point.lerpFactor,
                        point.offsetPoint,
                        vector3fMul,
                        data.topLevelSnapshot.bone.initialRotation,
                        vector3fMul,
                        rotScratch
                    )
                    mutableVector.set(vector3fMul)
                    mutableVector.setPercentCompleted(0.0f)
                }

                else -> {
                    mutableVector.set(point.getLerpPoint(evaluator))
                    val blendWeight2 = data.blendWeight
                    if (blendWeight2 != 1.0f) {
                        mutableVector.mul(blendWeight2)
                    }
                    mutableVector.setPercentCompleted(0.0f)
                }
            }
            return mutableVector
        }

        override fun getPosition(evaluator: ExpressionEvaluator<AnimationContext<*>>): TransitionVector3f? {
            val point = data.positionQueue ?: return null
            mutableVector.set(point.getLerpPoint(evaluator))
            var blendWeight = data.blendWeight
            when (point) {
                is ConstantPoint -> {
                    mutableVector.setPercentCompleted(point.percentCompleted)
                }

                else -> {
                    if (point is TransitionPoint) {
                        blendWeight = MathUtil.lerpValues(point.lerpFactor, 1.0f, blendWeight)
                    }
                    mutableVector.setPercentCompleted(0.0f)
                }
            }
            if (blendWeight != 1.0f) {
                mutableVector.mul(blendWeight)
            }
            return mutableVector
        }

        override fun getScale(evaluator: ExpressionEvaluator<AnimationContext<*>>): TransitionVector3f? {
            val point = data.scaleQueue ?: return null
            mutableVector.set(point.getLerpPoint(evaluator))
            var blendWeight = data.blendWeight
            if (point is ConstantPoint) {
                mutableVector.setPercentCompleted(point.percentCompleted)
            } else {
                if (point is TransitionPoint) {
                    blendWeight = MathUtil.lerpValues(point.lerpFactor, 1.0f, blendWeight)
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