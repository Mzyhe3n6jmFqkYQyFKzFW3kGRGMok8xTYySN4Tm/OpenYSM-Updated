@file:Suppress("unused")

package com.elfmcys.yesstevemodel.geckolib3.core.processor

import com.elfmcys.yesstevemodel.audio.AudioPlayerManager
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.controller.BoneTransformProvider
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.storage.IForeignVariableStorage
import com.elfmcys.yesstevemodel.geckolib3.core.molang.storage.VariableStorage
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneTopLevelSnapshot
import com.elfmcys.yesstevemodel.geckolib3.core.util.EulerNlerpScratch
import com.elfmcys.yesstevemodel.geckolib3.core.util.MathUtil
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import com.elfmcys.yesstevemodel.molang.runtime.Struct
import it.unimi.dsi.fastutil.ints.Int2ReferenceMaps
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap
import it.unimi.dsi.fastutil.objects.*
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.levelgen.RandomSupport
import net.minecraft.world.level.levelgen.XoroshiroRandomSource
import org.joml.Vector3f
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Consumer

class AnimationProcessor<TEntity : Entity>(private val animatable: AnimatableEntity<TEntity>) {
    private val bones: ReferenceArrayList<BoneTopLevelSnapshot> = ReferenceArrayList()
    private var initExpressions: Object2ReferenceMap<String, MutableList<IValue>> = Object2ReferenceMaps.emptyMap()
    private val boneById: Int2ReferenceOpenHashMap<BoneTopLevelSnapshot> = Int2ReferenceOpenHashMap()
    private val modelRendererList: ArrayDeque<BoneTopLevelSnapshot> = ArrayDeque()
    private val animationStorage: VariableStorage = VariableStorage()
    private val audioPlayerManager: AudioPlayerManager = AudioPlayerManager()
    private val random: RandomSource = XoroshiroRandomSource(RandomSupport.generateUniqueSeed())
    private val pendingExpressions: ConcurrentLinkedQueue<PendingExpression> = ConcurrentLinkedQueue()
    private var lastAudioTickTime: Float = 0.0f
    private var needsInit: Boolean = false
    private val transformConsumer: (BoneTransformProvider) -> Unit = ::applyTransform
    private var currentEvaluator: ExpressionEvaluator<AnimationContext<*>>? = null
    private var currentSeekTime: Float = 0.0f
    private var currentDeprecatedMode: Boolean = false
    private val rotScratch: EulerNlerpScratch = EulerNlerpScratch()

    fun tickAnimation(
        event: AnimationEvent<AnimatableEntity<TEntity>>,
        context: AnimationContext<*>,
        z: Boolean,
        z2: Boolean
    ) {
        context.setStorage(animationStorage)
        context.setRandom(random)
        context.setAudioPlayerManager(audioPlayerManager)
        val evaluator = ExpressionEvaluator.evaluator(context)
        val seekTime = event.currentTick
        if (seekTime - lastAudioTickTime >= 1200.0f) {
            audioPlayerManager.tick()
            lastAudioTickTime = seekTime
        } else if (lastAudioTickTime > seekTime) {
            lastAudioTickTime = seekTime
        }
        preProcess(evaluator)
        val manager = animatable.getAnimationData()
        currentEvaluator = evaluator
        currentSeekTime = seekTime
        for (controller in manager.getAnimationControllers()) {
            if (needsInit) {
                controller.init(bones, initExpressions)
            }
            if (z) {
                @Suppress("UNCHECKED_CAST")
                (controller as IAnimationController<AnimatableEntity<TEntity>>).process(event, evaluator, z2)
            }
            currentDeprecatedMode = controller.isDeprecatedMode()
            controller.forEachTransform(transformConsumer)
        }
        currentEvaluator = null
        needsInit = false
        val iterator = modelRendererList.iterator()
        while (iterator.hasNext()) {
            val topLevelSnapshot = iterator.next()
            var runningAnimation = false
            if (topLevelSnapshot.isCurrentlyRunningRotationAnimation) {
                runningAnimation = true
                topLevelSnapshot.isCurrentlyRunningRotationAnimation = false
                topLevelSnapshot.prevRotation = null
            } else {
                val prevRot = topLevelSnapshot.prevRotation
                    ?: Vector3f(topLevelSnapshot.rotation).also { topLevelSnapshot.prevRotation = it }
                val percentageReset =
                    (seekTime - topLevelSnapshot.mostRecentResetRotationTick) / manager.getResetSpeed()
                if (percentageReset < 1.0f) {
                    runningAnimation = true
                    MathUtil.nlerpEulerAngles(
                        percentageReset,
                        prevRot,
                        MathUtil.ZERO,
                        topLevelSnapshot.bone.getInitialRotation(),
                        topLevelSnapshot.rotation,
                        rotScratch
                    )
                } else {
                    topLevelSnapshot.rotation.set(MathUtil.ZERO)
                }
            }
            if (topLevelSnapshot.isCurrentlyRunningPositionAnimation) {
                runningAnimation = true
                topLevelSnapshot.isCurrentlyRunningPositionAnimation = false
                topLevelSnapshot.prevPosition = null
            } else {
                val prevPos = topLevelSnapshot.prevPosition
                    ?: Vector3f(topLevelSnapshot.position).also { topLevelSnapshot.prevPosition = it }
                val percentageReset =
                    (seekTime - topLevelSnapshot.mostRecentResetPositionTick) / manager.getResetSpeed()
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
                val prevScl = topLevelSnapshot.prevScale ?: Vector3f(topLevelSnapshot.scale).also {
                    topLevelSnapshot.prevScale = it
                }
                val percentageReset =
                    (seekTime - topLevelSnapshot.mostRecentResetScaleTick) / manager.getResetSpeed()
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

    private fun applyTransform(provider: BoneTransformProvider) {
        val snapshot = provider.getBoneTarget()
        if (!snapshot.isCurrentlyRunningAnimation) {
            snapshot.isCurrentlyRunningAnimation = true
            modelRendererList.add(snapshot)
        }
        val evaluator = currentEvaluator ?: return
        val seekTime = currentSeekTime

        val rot = provider.getRotation(evaluator)
        if (rot != null) {
            val vector3f = snapshot.currentValue
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
        val pos = provider.getPosition(evaluator)
        if (pos != null) {
            if (!snapshot.isCurrentlyRunningPositionAnimation) {
                snapshot.isCurrentlyRunningPositionAnimation = true
                snapshot.position.set(0.0f, 0.0f, 0.0f)
            }
            snapshot.mostRecentResetPositionTick = seekTime
            pos.applyLinearBlendTo(snapshot.position)
        }
        val scale = provider.getScale(evaluator)
        if (scale != null) {
            if (!snapshot.isCurrentlyRunningScaleAnimation) {
                snapshot.isCurrentlyRunningScaleAnimation = true
                snapshot.scale.set(1.0f, 1.0f, 1.0f)
            }
            snapshot.mostRecentResetScaleTick = seekTime
            scale.applyLinearBlendTo(snapshot.scale)
        }
    }

    fun getBone(i: Int): IBone? {
        val renderer = boneById.get(i)
        return renderer?.bone
    }

    fun reset() {
        boneById.clear()
        modelRendererList.clear()
        bones.clear()
        animationStorage.initialize(null)
        initExpressions = Object2ReferenceMaps.emptyMap()
        pendingExpressions.clear()
        audioPlayerManager.stopAll()
    }

    fun initBones(model: AnimatedGeoModel, object2ReferenceMap: Object2ReferenceMap<String, out List<IValue>>) {
        reset()
        if (model.bones().isNotEmpty()) {
            bones.ensureCapacity(model.bones().size)
            bones.add(null)
            val boneId = model.getGeoModel().bones[0].boneId
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
        val map = Object2ReferenceOpenHashMap<String, MutableList<IValue>>(object2ReferenceMap.size)
        for ((key, value) in object2ReferenceMap.object2ReferenceEntrySet()) {
            map[key] = ObjectArrayList(value)
        }
        initExpressions = map
    }

    fun setRoamingProperties(struct: Struct?) {
        if (struct != null) {
            animationStorage.setScoped(ROAMING_STRUCT_NAME, struct)
        }
    }

    fun isDisabled(): Boolean = bones.isEmpty

    private fun preProcess(evaluator: ExpressionEvaluator<AnimationContext<*>>) {
        val it = pendingExpressions.iterator()
        while (it.hasNext()) {
            val next = it.next()
            if (next.executeBeforeAnimation) {
                postProcess(next, evaluator)
                it.remove()
            }
        }
    }

    private fun postProcess(evaluator: ExpressionEvaluator<AnimationContext<*>>) {
        val it = pendingExpressions.iterator()
        while (it.hasNext()) {
            val next = it.next()
            if (!next.executeBeforeAnimation) {
                postProcess(next, evaluator)
                it.remove()
            }
        }
    }

    private fun postProcess(value: PendingExpression, evaluator: ExpressionEvaluator<AnimationContext<*>>) {
        try {
            evaluator.entity().setIsClientSide(value.isClientPlayer)
            val result = runCatching {
                value.value.evalSafe(evaluator)
            }.fold(
                onSuccess = { ret ->
                    if (ret == null) "null" else if (ret is String) "'$ret'" else ret.toString()
                },
                onFailure = { th ->
                    "Error: ${th.message}"
                }
            )
            value.callback?.accept(result)
        } finally {
            evaluator.entity().setIsClientSide(false)
        }
    }

    fun execute(
        value: IValue,
        isClientPlayer: Boolean,
        executeBeforeAnimation: Boolean,
        resultConsumer: Consumer<String>?
    ) {
        pendingExpressions.add(PendingExpression(value, isClientPlayer, executeBeforeAnimation, resultConsumer))
    }

    fun execute(
        value: IValue,
        isClientPlayer: Boolean,
        executeBeforeAnimation: Boolean,
        resultCallback: ((String) -> Unit)? = null
    ) {
        pendingExpressions.add(
            PendingExpression(
                value,
                isClientPlayer,
                executeBeforeAnimation,
                resultCallback?.let { Consumer(it) })
        )
    }

    fun getPublicVariableStorage(): IForeignVariableStorage = animationStorage

    fun forEachPropertyName(consumer: Consumer<String>) {
        animationStorage.forEachPropertyName(consumer::accept)
    }

    fun forEachPropertyName(action: (String) -> Unit) {
        animationStorage.forEachPropertyName(action)
    }

    private data class PendingExpression(
        val value: IValue,
        val isClientPlayer: Boolean,
        val executeBeforeAnimation: Boolean,
        val callback: Consumer<String>?
    )

    companion object {
        val ROAMING_STRUCT_NAME = StringPool.computeIfAbsent("roaming")
    }
}