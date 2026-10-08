@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.elfmcys.yesstevemodel.geckolib3.core

import com.elfmcys.yesstevemodel.audio.IAudioStreamFactory
import com.elfmcys.yesstevemodel.client.animation.molang.PhysicsManager
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable
import com.elfmcys.yesstevemodel.client.event.ClientTickEvent
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.enums.AnimationState
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.core.manager.AnimationData
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.storage.IForeignVariableStorage
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.geckolib3.core.processor.AnimationProcessor
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone
import com.elfmcys.yesstevemodel.geckolib3.core.util.RateLimiter
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel
import com.elfmcys.yesstevemodel.geckolib3.model.provider.data.EntityModelData
import com.elfmcys.yesstevemodel.util.log.ILogger
import com.google.common.collect.Maps
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import rip.ysm.api.entity.EntityDataBridge

abstract class AnimatableEntity<TEntity : Entity>(@JvmField val entity: TEntity) {
    @JvmField
    protected var positionTracker2: EntityFrameStateTracker<TEntity> = createPositionTracker(entity)

    @JvmField
    protected var currentModel2: AnimatedGeoModel? = null

    @JvmField
    var animationMap: Object2ReferenceMap<String, MutableList<IValue>>? = null

    @JvmField
    var wasAnimationActiveLastTick: Boolean = false

    @JvmField
    var hasUpdatedThisTick: Boolean = false

    @JvmField
    var isTickTriggered: Boolean = false

    @JvmField
    var wasEvaluatedLastFrame: Boolean = false

    @JvmField
    var seekTime: Float = 0.0f

    @JvmField
    val manager: AnimationData = AnimationData()

    @JvmField
    var lastTick: Float = -1.0f

    @JvmField
    var isFirstFrameAfterReset: Boolean = true

    @JvmField
    var needsReset: Boolean = false

    @JvmField
    protected var modelInitialized: Boolean = false

    @JvmField
    var animationStates: MutableMap<String, AnimationState> = Maps.newHashMap()

    @JvmField
    val animationProcessor: AnimationProcessor<TEntity> = AnimationProcessor(this)

    @JvmField
    val rateLimiter: RateLimiter = RateLimiter().apply { setRefreshRate(refreshRate) }

    @JvmField
    protected val defaultPhysicsManager: PhysicsManager = PhysicsManager()

    abstract val textureLocation: Identifier
    abstract val isModelReady: Boolean
    abstract val heightScale: Float
    abstract val widthScale: Float
    abstract fun getAnimation(str: String): Animation?
    abstract fun registerAnimationControllers()

    open fun reset() {
        currentModel2 = null
        animationMap = null
        animationProcessor.reset()
        defaultPhysicsManager.clear()
        rateLimiter.reset()
        manager.clear()
        positionTracker2.reset()
        lastTick = -1.0f
        wasAnimationActiveLastTick = false
        hasUpdatedThisTick = false
        isTickTriggered = false
        isFirstFrameAfterReset = true
        needsReset = false
        wasEvaluatedLastFrame = false
        seekTime = 0.0f
        animationStates.clear()
    }

    open fun createPositionTracker(entity: TEntity): EntityFrameStateTracker<TEntity> = EntityFrameStateTracker(entity)

    open val positionTracker: EntityFrameStateTracker<TEntity>
        get() = positionTracker2

    open fun getSeekTime(): Float = seekTime

    open fun addAnimationController(controller: IAnimationController<*>) {
        manager.addAnimationController(controller)
    }

    open fun getAnimationData(): AnimationData = manager

    open fun resolveExpression(str: String): IValue? = null

    open fun getAudioStreamFactory(str: String): IAudioStreamFactory? = null

    fun getAnimationExpressions(str: String): MutableList<IValue>? = animationMap?.get(str)

    open val physicsManager: PhysicsManager
        get() = defaultPhysicsManager

    open fun getAnimationEntries(str: String): AnimationController? = null

    open val textureIndex: Int
        get() = 0

    open fun getScale(): Float = 0.15f

    open fun setupAnim(seekTime: Float, isFirstPerson: Boolean) {}
    open fun afterSetupAnim(seekTime: Float, isFirstPerson: Boolean) {}

    open fun hasCustomTexture(): Boolean = false

    open fun getBone(i: Int): IBone? = animationProcessor.getBone(i)

    open fun shouldRenderOverlay(): Boolean = true

    open val refreshRate: Int
        get() {
            val player = Minecraft.getInstance().player
            if (player != null && player != entity) {
                val vec3Position = player.position()
                if (vec3Position.x != 0.0 || vec3Position.y != 0.0 || vec3Position.z != 0.0) {
                    if (!isFirstFrameAfterReset) return 10
                    val fDistanceTo = player.distanceTo(entity)
                    when {
                        fDistanceTo > 64.0f -> return 30
                        fDistanceTo > 40.0f -> return 60
                    }
                }
            }
            return ClientTickEvent.refreshRate
        }

    fun processAnimation(partialTick: Float): AnimationEvent<*>? =
        processAnimationImpl(partialTick, ModelPreviewRenderer.isFirstPersonOnRenderThread())

    open fun processAnimationImpl(partialTick: Float, z: Boolean): AnimationEvent<*>? {
        if (currentModel2 == null) return null
        val livingEntity = entity as? LivingEntity
        val tickCount = if (this is IPreviewAnimatable) ClientTickEvent.tickCount else entity.tickCount
        val frameTime =
            if (partialTick != 1.0f) partialTick else Minecraft.getInstance().deltaTracker.getGameTimeDeltaPartialTick(
                false
            )
        val shouldSit =
            entity.isPassenger && entity.vehicle != null && entity.vehicle?.let { EntityDataBridge.shouldRiderSit(it) } == true
        var limbSwingAmount = 0.0f
        var limbSwing = 0.0f
        if (!shouldSit && entity.isAlive && livingEntity != null) {
            limbSwingAmount = livingEntity.walkAnimation.speed(partialTick)
            limbSwing = livingEntity.walkAnimation.position(partialTick)
            if (livingEntity.isBaby) limbSwing *= 3.0f
        }
        val modelData = EntityModelData()
        modelData.isSitting = shouldSit
        var lerpBodyRot = 0.0f
        var lerpHeadRot = 0.0f
        var netHeadYaw = 0.0f
        if (livingEntity != null) {
            modelData.isChild = livingEntity.isBaby
            lerpBodyRot = Mth.rotLerp(partialTick, livingEntity.yBodyRotO, livingEntity.yBodyRot)
            lerpHeadRot = Mth.rotLerp(partialTick, livingEntity.yHeadRotO, livingEntity.yHeadRot)
            netHeadYaw = lerpHeadRot - lerpBodyRot
        }
        val vehicle = entity.vehicle
        if (shouldSit && vehicle is LivingEntity) {
            lerpBodyRot = Mth.rotLerp(partialTick, vehicle.yBodyRotO, vehicle.yBodyRot)
            val clampedHeadYaw = Mth.clamp(Mth.wrapDegrees(lerpHeadRot - lerpBodyRot), -85.0f, 85.0f)
            lerpBodyRot = lerpHeadRot - clampedHeadYaw
            if (clampedHeadYaw * clampedHeadYaw > 2500f) {
                lerpBodyRot += clampedHeadYaw * 0.2f
            }
            netHeadYaw = lerpHeadRot - lerpBodyRot
        }
        modelData.rawHeadPitch = Mth.lerp(partialTick, entity.xRotO, entity.xRot)
        modelData.headPitch = -modelData.rawHeadPitch
        modelData.rawNetHeadYaw = netHeadYaw
        modelData.netHeadYaw = -Mth.clamp(Mth.wrapDegrees(netHeadYaw), -85.0f, 85.0f)
        modelData.lerpBodyRot = lerpBodyRot
        modelData.lerpedAge = tickCount + partialTick
        val event = AnimationEvent(
            this,
            limbSwing,
            limbSwingAmount,
            tickCount,
            partialTick,
            frameTime,
            limbSwingAmount <= -getScale() || limbSwingAmount <= getScale(),
            z,
            modelData
        )
        val context = AnimationContext(entity, this, event, modelData)
        logger?.let { context.setLogger(it) }
        setCustomAnimations(context, event)
        return event
    }

    open fun setCustomAnimations(ctx: AnimationContext<*>, event: AnimationEvent<AnimatableEntity<TEntity>>) {
        var currentTick = event.currentTick
        val z = !shouldSkipAnimation(event)
        if (currentTick > lastTick) {
            hasUpdatedThisTick = false
            isTickTriggered = false
            lastTick = currentTick
            rateLimiter.setRefreshRate(refreshRate)
            isFirstFrameAfterReset = needsReset
            needsReset = false
        } else {
            currentTick = lastTick
        }
        if (manager.startTick == -1.0f) {
            manager.startTick = currentTick
        } else {
            val f2 = currentTick - manager.startTick
            val f3 = f2 - manager.limbSwing
            if (f3 > 0.0f) {
                manager.limbSwing = f2
                seekTime += f3
            }
        }
        event.currentTick = seekTime
        if (!animationProcessor.isDisabled()) {
            isTickTriggered = isTickTriggered or rateLimiter.request(seekTime / 20.0f)
            val z2 = isTickTriggered && !hasUpdatedThisTick || wasAnimationActiveLastTick || z
            val z3 =
                (!z || seekTime == 0.0f && !hasUpdatedThisTick) && isTickTriggered && !hasUpdatedThisTick
            resetHeadTracking(wasEvaluatedLastFrame)
            if (z2) {
                if (z3) {
                    hasUpdatedThisTick = true
                    positionTracker2.updateState(event.getTickCount(), seekTime, event.getFrameTime())
                }
                physicsManager.update(seekTime)
                setupAnim(seekTime, z3)
                getEvaluationContext().tickAnimation(event, ctx, z3, shouldRenderOverlay())
                afterSetupAnim(seekTime, z3)
                wasAnimationActiveLastTick = z
            }
            applyHeadTracking(event, z2)
            wasEvaluatedLastFrame = z2
        }
    }

    open fun applyHeadTracking(event: AnimationEvent<AnimatableEntity<TEntity>>, z: Boolean) {}
    open fun resetHeadTracking(wasAnimEvaluated: Boolean) {}

    open fun getEvaluationContext(): AnimationProcessor<TEntity> = animationProcessor

    open fun initAnimationControllers(
        model: GeoModel,
        object2ReferenceMap: Object2ReferenceMap<String, MutableList<IValue>>
    ) {
        reset()
        val animModel = AnimatedGeoModel(model)
        currentModel2 = animModel
        animationMap = object2ReferenceMap
        registerAnimationControllers()
        animationProcessor.initBones(animModel, object2ReferenceMap)
        currentModel = animModel
    }

    open fun clearAnimationControllers() {
        currentModel2?.let {
            val model = it.getGeoModel()
            val object2ReferenceMap: Object2ReferenceMap<String, MutableList<IValue>>? = animationMap
            reset()
            initAnimationControllers(model, object2ReferenceMap ?: return)
        }
    }

    open var currentModel: AnimatedGeoModel?
        get() = currentModel2
        set(value) {}

    open fun shouldSkipAnimation(event: AnimationEvent<*>): Boolean = true

    open fun resetAnimationState() {
        needsReset = true
    }

    open fun executeExpression(
        value: IValue,
        isClientPlayer: Boolean,
        executeBeforeAnimation: Boolean,
        func: ((String) -> Unit)?
    ) {
        if (func != null) {
            animationProcessor.execute(value, isClientPlayer, executeBeforeAnimation, func)
        } else {
            animationProcessor.execute(value, isClientPlayer, executeBeforeAnimation, null)
        }
    }

    open val propertyGetter: IForeignVariableStorage
        get() = animationProcessor.publicVariableStorage

    open fun markModelInitialized() {
        modelInitialized = true
    }

    open val isModelInitialized: Boolean
        get() = modelInitialized

    open val logger: ILogger?
        get() = null

    open val isDebugMode: Boolean
        get() = Minecraft.getInstance().level == entity.level() && !entity.isRemoved

    open fun setAnimationState(name: String, state: AnimationState) {
        animationStates[name] = state
    }

    open fun getAnimationState(name: String): AnimationState = animationStates.getOrDefault(name, AnimationState.IDLE)

    fun interface AnimationControllerVisitor : ((IAnimationController<*>) -> Unit) -> Unit
}