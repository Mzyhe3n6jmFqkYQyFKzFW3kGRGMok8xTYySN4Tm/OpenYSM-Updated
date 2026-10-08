package com.elfmcys.yesstevemodel.client.entity

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.audio.AudioCodec
import com.elfmcys.yesstevemodel.audio.AudioStreamCache
import com.elfmcys.yesstevemodel.audio.IAudioStreamFactory
import com.elfmcys.yesstevemodel.audio.IAudioStreamProvider
import com.elfmcys.yesstevemodel.client.ClientModelManager
import com.elfmcys.yesstevemodel.client.animation.molang.MolangEventDispatcher
import com.elfmcys.yesstevemodel.client.animation.molang.MolangWatchRegistry
import com.elfmcys.yesstevemodel.client.animation.molang.PhysicsManager
import com.elfmcys.yesstevemodel.client.model.ModelAssembly
import com.elfmcys.yesstevemodel.client.renderer.AnimationDebugOverlay
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel
import com.elfmcys.yesstevemodel.util.YSMThreadPool
import com.elfmcys.yesstevemodel.util.log.ChatLogger
import com.elfmcys.yesstevemodel.util.log.ILogger
import com.mojang.blaze3d.systems.RenderSystem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import net.minecraft.world.entity.Entity
import rip.ysm.compat.oculus.OculusCompat
import java.lang.invoke.VarHandle

abstract class GeoEntity<T : Entity>(t: T, registerWithCache: Boolean) : AnimatableEntity<T>(t) {
    private var _modelId: String = "default"
    var modelAssembly: ModelAssembly? = null
        private set
    var renderShape: ModelWrapper? = null
        private set
    private var loaded: Boolean = false
    private var updateTicks: Int = 0
    private var bones: PhysicsManager? = null
    private var boneLookup: MolangWatchRegistry? = null
    var renderLayers: List<IValue>? = null
        private set
    private var modelDeferred: Deferred<AnimationEvent<*>?>? = null

    init {
        if (registerWithCache) {
            EntityRenderCache.register(this)
        }
    }

    abstract fun buildRenderShape(modelAssembly: ModelAssembly, isDefault: Boolean): ModelWrapper?
    abstract val model: GeoModel?

    override val physicsManager: PhysicsManager
        get() {
            if (ModelPreviewRenderer.isFirstPerson() || ModelPreviewRenderer.isExtraPlayer())
                return defaultPhysicsManager
            val currentBones = bones
            if (currentBones == null) {
                val newBones = PhysicsManager()
                bones = newBones
                return newBones
            }
            return currentBones
        }

    open fun setBoneLookup(watchRegistry: MolangWatchRegistry?) {
        boneLookup = watchRegistry
    }

    override fun setupAnim(seekTime: Float, isFirstPerson: Boolean) {
        super.setupAnim(seekTime, isFirstPerson)
        val lookup = boneLookup
        if (lookup != null) {
            val processor = evaluationContext
            processor.execute({ evaluator ->
                lookup.evaluatePreAnimation(evaluator)
                null
            }, false, true) {}
            processor.execute({ evaluator ->
                lookup.evaluatePostAnimation(evaluator)
                null
            }, false, false) {}
        }
    }

    open fun tickModel() {
        if (updateTicks < entity.tickCount) {
            refreshModel()
            updateTicks = entity.tickCount
        }
    }

    private fun refreshModel() {
        ClientModelManager.getModelContext(_modelId)?.let { assembly ->
            val shape = renderShape
            if (shape == null || shape.isDefault || assembly != shape.context) {
                renderShape = buildRenderShape(assembly, false)
            }
        } ?: run {
            val localAssembly = ClientModelManager.localModelContext
            val shape = renderShape
            if (shape == null || !shape.isDefault || localAssembly != shape.context) {
                renderShape = buildRenderShape(localAssembly, true)
            }
        }

        val shape = renderShape
        if (shape != null) {
            if ((shape.context != modelAssembly || shape.isDefault != loaded) && shape.isValid) {
                modelAssembly = shape.context
                loaded = shape.isDefault
                modelAssembly?.let { onModelLoaded(it) }
                model?.let { initAnimationControllers(it, shape.context.expressionCache.events) }
                return
            }
            return
        }
        if (modelAssembly != null) {
            clearModel()
        }
    }

    open fun onModelLoaded(modelAssembly: ModelAssembly) {
        renderShape?.audioProvider = AudioStreamCache.getOrCreateProvider(modelAssembly)
        renderLayers = modelAssembly.expressionCache.events[MolangEventDispatcher.DEFER]
    }

    open fun clearModel() {
        modelAssembly = null
        renderLayers = null
        renderShape = null
        loaded = false
        reset()
    }

    override fun reset() {
        super.reset()
        bones = null
        updateTicks = 0
    }

    open fun resetModel() {
        _modelId = "default"
        modelInitialized = false
        clearModel()
    }

    var modelId: String
        get() = _modelId
        set(value) {
            _modelId = value
            refreshModel()
        }

    override val isModelReady: Boolean
        get() {
            val shape = renderShape
            return shape != null && !shape.isDefault && shape.isValid
        }

    override fun shouldSkipAnimation(event: AnimationEvent<*>): Boolean =
        event.isFirstPerson || OculusCompat.isPBRActive()

    override fun resolveExpression(str: String): IValue? = modelAssembly?.expressionCache?.functions?.get(str)

    override fun getAudioStreamFactory(str: String): IAudioStreamFactory? {
        val shape = renderShape ?: return null
        val provider = shape.audioProvider ?: return null
        val trackData = modelAssembly?.expressionCache?.soundEffects?.get(str)
        if (trackData?.data != null && trackData.codec != AudioCodec.UNDEFINED)
            return IAudioStreamFactory { provider.createAudioStream(trackData) }
        return null
    }

    override val logger: ILogger?
        get() {
            if (AnimationDebugOverlay.isDebugActive()) return ChatLogger
            return null
        }

    open fun submitAsyncUpdate(partialTick: Float) {
        VarHandle.storeStoreFence()
        modelDeferred = YSMThreadPool.async {
            runCatching {
                val event = super.processAnimationImpl(partialTick, true)
                VarHandle.storeStoreFence()
                event
            }.onFailure {
                VarHandle.storeStoreFence()
            }.getOrThrow()
        }
    }

    override fun processAnimationImpl(partialTick: Float, z: Boolean): AnimationEvent<*>? {
        RenderSystem.assertOnRenderThread()
        if (z && modelDeferred != null) return awaitAsyncResult()
        awaitAsyncResult()
        return super.processAnimationImpl(partialTick, z)
    }

    open fun awaitAsyncResult(): AnimationEvent<*>? {
        val future = modelDeferred ?: return null
        modelDeferred = null
        return runCatching {
            runBlocking { future.await() }.also {
                VarHandle.loadLoadFence()
            }
        }.onFailure {
            if (it is InterruptedException || it is CancellationException) return@onFailure
            Constants.LOGGER.error("Failed to get model future", it)
        }.getOrNull()
    }

    open fun supportsAsync(): Boolean = true

    open class ModelWrapper(
        val context: ModelAssembly,
        val isDefault: Boolean
    ) {
        var audioProvider: IAudioStreamProvider? = null

        open val isValid: Boolean
            get() = true
    }
}