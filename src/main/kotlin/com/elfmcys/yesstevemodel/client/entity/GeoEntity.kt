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
import com.elfmcys.yesstevemodel.util.UnsafeUtil
import com.elfmcys.yesstevemodel.util.YSMThreadPool
import com.elfmcys.yesstevemodel.util.log.ChatLogger
import com.elfmcys.yesstevemodel.util.log.ILogger
import com.mojang.blaze3d.systems.RenderSystem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import net.minecraft.world.entity.Entity
import rip.ysm.compat.oculus.OculusCompat

abstract class GeoEntity<T : Entity>(t: T, registerWithCache: Boolean) : AnimatableEntity<T>(t) {
    private var modelId: String = "default"
    private var modelAssembly: ModelAssembly? = null
    private var renderShape: ModelWrapper? = null
    private var loaded: Boolean = false
    private var updateTicks: Int = 0
    private var bones: PhysicsManager? = null
    private var boneLookup: MolangWatchRegistry? = null
    private var renderLayers: List<IValue>? = null
    private var modelDeferred: Deferred<AnimationEvent<*>?>? = null

    init {
        if (registerWithCache) {
            EntityRenderCache.register(this)
        }
    }

    abstract fun buildRenderShape(modelAssembly: ModelAssembly, isDefault: Boolean): ModelWrapper?
    abstract fun getAnimationProcessor(): GeoModel

    override fun getPhysicsManager(): PhysicsManager {
        if (ModelPreviewRenderer.isFirstPerson() || ModelPreviewRenderer.isExtraPlayer()) {
            return defaultPhysicsManager
        }
        val currentBones = bones
        if (currentBones == null) {
            val newBones = PhysicsManager()
            bones = newBones
            return newBones
        }
        return currentBones
    }

    open fun getRenderLayers(): List<IValue>? {
        return renderLayers
    }

    open fun setBoneLookup(watchRegistry: MolangWatchRegistry?) {
        boneLookup = watchRegistry
    }

    override fun setupAnim(seekTime: Float, isFirstPerson: Boolean) {
        super.setupAnim(seekTime, isFirstPerson)
        val lookup = boneLookup
        if (lookup != null) {
            val processor = getEvaluationContext()
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

    fun getModelAssembly(): ModelAssembly? {
        return modelAssembly
    }

    fun setModelId(str: String) {
        modelId = str
        refreshModel()
    }

    private fun refreshModel() {
        ClientModelManager.getModelContext(modelId)?.let { assembly ->
            val shape = renderShape
            if (shape == null || shape.isDefault || assembly != shape.context) {
                renderShape = buildRenderShape(assembly, false)
            }
        } ?: run {
            val localAssembly = ClientModelManager.getLocalModelContext()
            val shape = renderShape
            if (shape == null || !shape.isDefault || localAssembly != shape.context) {
                renderShape = buildRenderShape(localAssembly, true)
            }
        }

        val shape = renderShape
        if (shape != null) {
            if ((shape.context != modelAssembly || shape.isDefault != loaded) && shape.isValid()) {
                modelAssembly = shape.context
                loaded = shape.isDefault
                modelAssembly?.let { onModelLoaded(it) }
                initAnimationControllers(getAnimationProcessor(), shape.context.expressionCache.events)
                return
            }
            return
        }
        if (modelAssembly != null) {
            clearModel()
        }
    }

    fun getRenderShape(): ModelWrapper? = renderShape

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
        modelId = "default"
        modelInitialized = false
        clearModel()
    }

    fun getModelId(): String = modelId

    override fun isModelReady(): Boolean {
        val shape = renderShape
        return shape != null && !shape.isDefault && shape.isValid()
    }

    override fun shouldSkipAnimation(event: AnimationEvent<*>): Boolean =
        event.isFirstPerson() || OculusCompat.isPBRActive()

    override fun resolveExpression(str: String): IValue? = getModelAssembly()?.expressionCache?.functions?.get(str)

    override fun getAudioStreamFactory(str: String): IAudioStreamFactory? {
        val shape = renderShape ?: return null
        val provider = shape.audioProvider ?: return null
        val trackData = getModelAssembly()?.expressionCache?.soundEffects?.get(str)
        if (trackData?.data != null && trackData.codec != AudioCodec.UNDEFINED)
            return IAudioStreamFactory { provider.createAudioStream(trackData) }
        return null
    }

    override fun getLogger(): ILogger? {
        if (AnimationDebugOverlay.isDebugActive()) return ChatLogger
        return null
    }

    // TODO: 'fun storeFence(): Unit' is deprecated. Deprecated in Java.
    open fun submitAsyncUpdate(partialTick: Float) {
        UnsafeUtil.getUnsafe().storeFence()
        modelDeferred = YSMThreadPool.async {
            runCatching {
                val event = super.processAnimationImpl(partialTick, true)
                UnsafeUtil.getUnsafe().storeFence()
                event
            }.onFailure {
                UnsafeUtil.getUnsafe().storeFence()
            }.getOrThrow()
        }
    }

    override fun processAnimationImpl(partialTick: Float, isFirstPerson: Boolean): AnimationEvent<*>? {
        RenderSystem.assertOnRenderThread()
        if (isFirstPerson && modelDeferred != null) return awaitAsyncResult()
        awaitAsyncResult()
        return super.processAnimationImpl(partialTick, isFirstPerson)
    }

    open fun awaitAsyncResult(): AnimationEvent<*>? {
        val future = modelDeferred ?: return null
        modelDeferred = null
        return runCatching {
            runBlocking { future.await() }.also {
                UnsafeUtil.getUnsafe().loadFence()
            }
        }.onFailure {
            if (it is InterruptedException || it is CancellationException) return@onFailure
            Constants.LOGGER.error("Failed to get model future", it)
        }.getOrNull()
    }

    open fun supportsAsync(): Boolean = true

    open class ModelWrapper(
        @JvmField val context: ModelAssembly,
        @JvmField val isDefault: Boolean
    ) {
        @JvmField
        var audioProvider: IAudioStreamProvider? = null

        open fun isValid(): Boolean = true
    }
}