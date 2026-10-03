package com.elfmcys.yesstevemodel.geckolib3.core.processor

import com.elfmcys.yesstevemodel.audio.AudioPlayerManager
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.controller.BoneTransformProvider
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.core.manager.AnimationData
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.storage.IForeignVariableStorage
import com.elfmcys.yesstevemodel.geckolib3.core.molang.storage.VariableStorage
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneTopLevelSnapshot
import com.elfmcys.yesstevemodel.geckolib3.core.util.EulerNlerpScratch
import com.elfmcys.yesstevemodel.geckolib3.core.util.MathUtil
import com.elfmcys.yesstevemodel.geckolib3.core.util.TransitionVector3f
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import com.elfmcys.yesstevemodel.molang.runtime.Struct
import it.unimi.dsi.fastutil.ints.Int2ReferenceMaps
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.levelgen.RandomSupport
import net.minecraft.world.level.levelgen.XoroshiroRandomSource
import org.joml.Vector3f
import java.util.ArrayDeque
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Consumer

open class AnimationProcessor<TEntity : Entity>(val animatable: AnimatableEntity<TEntity>) {
    val bones: ReferenceArrayList<BoneTopLevelSnapshot> = ReferenceArrayList()
    var initExpressions: Object2ReferenceMap<String, MutableList<IValue>> = Object2ReferenceMaps.emptyMap()
    val boneById: Int2ReferenceOpenHashMap<BoneTopLevelSnapshot> = Int2ReferenceOpenHashMap()
    val modelRendererList: ArrayDeque<BoneTopLevelSnapshot> = ArrayDeque()
    val animationStorage: VariableStorage = VariableStorage()
    val audioPlayerManager: AudioPlayerManager = AudioPlayerManager()
    val random: RandomSource = XoroshiroRandomSource(RandomSupport.generateUniqueSeed())
    val pendingExpressions: ConcurrentLinkedQueue<PendingExpression> = ConcurrentLinkedQueue()
    var lastAudioTickTime: Float = 0.0f
    var needsInit: Boolean = false
    val transformConsumer: Consumer<BoneTransformProvider> = Consumer { provider -> applyTransform(provider) }
    var currentEvaluator: ExpressionEvaluator<AnimationContext<*>>? = null
    var currentSeekTime: Float = 0.0f
    var currentDeprecatedMode: Boolean = false
    val rotScratch: EulerNlerpScratch = EulerNlerpScratch()

    open fun tickAnimation(event: AnimationEvent<AnimatableEntity<TEntity>>, context: AnimationContext<*>, z: Boolean, z2: Boolean) {
        context.setStorage(animationStorage)
        context.setRandom(random)
        context.setAudioPlayerManager(audioPlayerManager)
        val evaluator: ExpressionEvaluator<AnimationContext<*>> = ExpressionEvaluator.evaluator(context)
        val seekTime: Float = event.currentTick
        if (seekTime - lastAudioTickTime >= 1200.0f) {
            audioPlayerManager.tick()
            lastAudioTickTime = seekTime
        } else if (lastAudioTickTime > seekTime) {
            lastAudioTickTime = seekTime
        }
        preProcess(evaluator)
        val manager: AnimationData = animatable.getAnimationData()
        currentEvaluator = evaluator
        currentSeekTime = seekTime
        for (controller in manager.getAnimationControllers()) {
            if (needsInit) {
                controller.init(bones, initExpressions)
            }
            if (z) {
                @Suppress("UNCHECKED_CAST")
                (controller as com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController<AnimatableEntity<TEntity>>).process(event, evaluator, z2)
            }
            currentDeprecatedMode = controller.isDeprecatedMode()
            controller.forEachTransform(transformConsumer)
        }
        currentEvaluator = null
        needsInit = false
        val iterator = modelRendererList.iterator()
        while (iterator.hasNext()) {
            val topLevelSnapshot: BoneTopLevelSnapshot = iterator.next()
            var runningAnimation = false
            if (topLevelSnapshot.isCurrentlyRunningRotationAnimation) {
                runningAnimation = true
                topLevelSnapshot.isCurrentlyRunningRotationAnimation = false
                topLevelSnapshot.prevRotation = null
            } else {
                val prevRot = topLevelSnapshot.prevRotation ?: Vector3f(topLevelSnapshot.rotation).also { topLevelSnapshot.prevRotation = it }
                val percentageReset: Float = (seekTime - topLevelSnapshot.mostRecentResetRotationTick) / manager.getResetSpeed()
                if (percentageReset < 1.0f) {
                    runningAnimation = true
                    MathUtil.nlerpEulerAngles(percentageReset, prevRot, MathUtil.ZERO, topLevelSnapshot.bone.getInitialRotation(), topLevelSnapshot.rotation, rotScratch)
                } else {
                    topLevelSnapshot.rotation.set(MathUtil.ZERO)
                }
            }
            if (topLevelSnapshot.isCurrentlyRunningPositionAnimation) {
                runningAnimation = true
                topLevelSnapshot.isCurrentlyRunningPositionAnimation = false
                topLevelSnapshot.prevPosition = null
            } else {
                val prevPos = topLevelSnapshot.prevPosition ?: Vector3f(topLevelSnapshot.position).also { topLevelSnapshot.prevPosition = it }
                val percentageReset: Float = (seekTime - topLevelSnapshot.mostRecentResetPositionTick) / manager.getResetSpeed()
                if (percentageReset < 1.0f) {
                    runningAnimation = true
                    MathUtil.lerpValues(percentageReset, prevPos, MathUtil.ZERO, topLevelSnapshot.position)
                } else {
                    topLevelSnapshot.position.set(0.0f, 0.0f, 0.0f)
                }
            }
            if (topLevelSnapshot.isCurrentlyRunningScaleAnimation) {
                runningAnimation = true
                topLevelSnapshot.isCurrentlyRunningScaleAnimation = false
                topLevelSnapshot.prevScale = null
            } else {
                val prevScl = topLevelSnapshot.prevScale ?: Vector3f(topLevelSnapshot.scale).also { topLevelSnapshot.prevScale = it }
                val percentageReset: Float = (seekTime - topLevelSnapshot.mostRecentResetScaleTick) / manager.getResetSpeed()
                if (percentageReset < 1.0f) {
                    runningAnimation = true
                    MathUtil.lerpValues(percentageReset, prevScl, MathUtil.ONE, topLevelSnapshot.scale)
                } else {
                    topLevelSnapshot.scale.set(1.0f, 1.0f, 1.0f)
                }
            }
            topLevelSnapshot.reset()
            if (!runningAnimation) {
                topLevelSnapshot.isCurrentlyRunningAnimation = false
                iterator.remove()
            }
        }
        context.setPlaybackFlags(null)
        context.setAnimationControllerContext(null)
        postProcess(evaluator)
    }

    open fun applyTransform(provider: BoneTransformProvider) {
        val snapshot: BoneTopLevelSnapshot = provider.getBoneTarget()
        if (!snapshot.isCurrentlyRunningAnimation) {
            snapshot.isCurrentlyRunningAnimation = true
            modelRendererList.add(snapshot)
        }
        val evaluator: ExpressionEvaluator<AnimationContext<*>>? = currentEvaluator
        val seekTime: Float = currentSeekTime
        if (evaluator != null) {
            val rot: TransitionVector3f? = provider.getRotation(evaluator)
            if (rot != null) {
                val vector3f: Vector3f = snapshot.currentValue
                if (!snapshot.isCurrentlyRunningRotationAnimation) {
                    snapshot.isCurrentlyRunningRotationAnimation = true
                    snapshot.rotation.set(0.0f, 0.0f, 0.0f)
                }
                snapshot.mostRecentResetRotationTick = seekTime
                if (currentDeprecatedMode) {
                    vector3f.add(rot)
                    snapshot.rotation.set(vector3f)
                } else {
                    rot.applyRotationBlendTo(snapshot.rotation, snapshot.bone.getInitialRotation(), rotScratch)
                    vector3f.set(snapshot.rotation)
                }
            }
            val pos: TransitionVector3f? = provider.getPosition(evaluator)
            if (pos != null) {
                if (!snapshot.isCurrentlyRunningPositionAnimation) {
                    snapshot.isCurrentlyRunningPositionAnimation = true
                    snapshot.position.set(0.0f, 0.0f, 0.0f)
                }
                snapshot.mostRecentResetPositionTick = seekTime
                pos.applyLinearBlendTo(snapshot.position)
            }
            val scale: TransitionVector3f? = provider.getScale(evaluator)
            if (scale != null) {
                if (!snapshot.isCurrentlyRunningScaleAnimation) {
                    snapshot.isCurrentlyRunningScaleAnimation = true
                    snapshot.scale.set(1.0f, 1.0f, 1.0f)
                }
                snapshot.mostRecentResetScaleTick = seekTime
                scale.applyLinearBlendTo(snapshot.scale)
            }
        }
    }

    open fun getBone(i: Int): IBone? {
        val renderer: BoneTopLevelSnapshot? = boneById.get(i)
        return renderer?.bone
    }

    open fun reset() {
        boneById.clear()
        modelRendererList.clear()
        bones.clear()
        animationStorage.initialize(null)
        initExpressions = Object2ReferenceMaps.emptyMap()
        pendingExpressions.clear()
        audioPlayerManager.stopAll()
    }

    open fun initBones(model: AnimatedGeoModel, object2ReferenceMap: Object2ReferenceMap<String, MutableList<IValue>>) {
        reset()
        if (model.bones().isNotEmpty()) {
            bones.ensureCapacity(model.bones().size)
            bones.add(null)
            val boneId: Int = model.getGeoModel().bones[0].getBoneId()
            Int2ReferenceMaps.fastForEach(model.bones()) { entry ->
                val boneTopLevelSnapshot = BoneTopLevelSnapshot(entry.value)
                boneById.put(entry.value.getBoneId(), boneTopLevelSnapshot)
                if (entry.intKey == boneId) {
                    bones[0] = boneTopLevelSnapshot
                } else {
                    bones.add(boneTopLevelSnapshot)
                }
            }
        }
        needsInit = true
        initExpressions = object2ReferenceMap
    }

    open fun setRoamingProperties(struct: Struct?) {
        if (struct != null) {
            animationStorage.setScoped(ROAMING_STRUCT_NAME, struct)
        }
    }

    open fun isDisabled(): Boolean = bones.isEmpty()

    private fun preProcess(evaluator: ExpressionEvaluator<AnimationContext<*>>) {
        val it = pendingExpressions.iterator()
        while (it.hasNext()) {
            val next: PendingExpression = it.next()
            if (next.executeBeforeAnimation) {
                postProcess(next, evaluator)
                it.remove()
            }
        }
    }

    private fun postProcess(evaluator: ExpressionEvaluator<AnimationContext<*>>) {
        val it = pendingExpressions.iterator()
        while (it.hasNext()) {
            val next: PendingExpression = it.next()
            if (!next.executeBeforeAnimation) {
                postProcess(next, evaluator)
                it.remove()
            }
        }
    }

    private fun postProcess(value: PendingExpression, evaluator: ExpressionEvaluator<AnimationContext<*>>) {
        try {
            var ret: Any?
            var result: String? = null
            try {
                evaluator.entity().setIsClientSide(value.isClientPlayer)
                ret = value.value.evalSafe(evaluator)
            } catch (th: Throwable) {
                ret = "Error: " + th.message
                result = ret
                evaluator.entity().setIsClientSide(false)
            }
            if (value.callback == null) {
                evaluator.entity().setIsClientSide(false)
                return
            }
            result = if (ret == null) "null" else if (ret is String) "'$ret'" else ret.toString()
            evaluator.entity().setIsClientSide(false)
            value.callback.accept(result)
        } catch (th2: Throwable) {
            evaluator.entity().setIsClientSide(false)
            throw th2
        }
    }

    open fun execute(value: IValue, isClientPlayer: Boolean, executeBeforeAnimation: Boolean, resultConsumer: Consumer<String>?) {
        pendingExpressions.add(PendingExpression(value, isClientPlayer, executeBeforeAnimation, resultConsumer))
    }

    open fun getPublicVariableStorage(): IForeignVariableStorage = animationStorage

    open fun forEachPropertyName(consumer: Consumer<String>) {
        animationStorage.forEachPropertyName(consumer)
    }

    data class PendingExpression(
        val value: IValue,
        val isClientPlayer: Boolean,
        val executeBeforeAnimation: Boolean,
        val callback: Consumer<String>?
    )

    companion object {
        @JvmField val ROAMING_STRUCT_NAME: Int = StringPool.computeIfAbsent("roaming")
    }
}