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
import net.minecraft.world.phys.Vec3
import rip.ysm.api.entity.EntityDataBridge
import java.util.Optional
import java.util.function.Consumer

abstract class AnimatableEntity<TEntity : Entity>(val entity: TEntity) {
    var positionTracker: EntityFrameStateTracker<TEntity> = createPositionTracker(entity)
    var currentModel: AnimatedGeoModel? = null
    var animationMap: Object2ReferenceMap<String, MutableList<IValue>>? = null
    var wasAnimationActiveLastTick: Boolean = false
    var hasUpdatedThisTick: Boolean = false
    var isTickTriggered: Boolean = false
    var wasEvaluatedLastFrame: Boolean = false
    var seekTime: Float = 0.0f
    val manager: AnimationData = AnimationData()
    var lastTick: Float = -1.0f
    var isFirstFrameAfterReset: Boolean = true
    var needsReset: Boolean = false
    var modelInitialized: Boolean = false
    var animationStates: MutableMap<String, AnimationState> = Maps.newHashMap()
    val animationProcessor: AnimationProcessor<TEntity> = AnimationProcessor(this)
    val rateLimiter: RateLimiter = RateLimiter().apply { setRefreshRate(getRefreshRate()) }
    val physicsManager: PhysicsManager = PhysicsManager()

    abstract fun getTextureLocation(): Identifier
    abstract fun isModelReady(): Boolean
    abstract fun getHeightScale(): Float
    abstract fun getWidthScale(): Float
    abstract fun getAnimation(str: String): Animation?
    abstract fun registerAnimationControllers()

    open fun reset() {
        currentModel = null
        animationMap = null
        animationProcessor.reset()
        physicsManager.clear()
        rateLimiter.reset()
        manager.clear()
        positionTracker.reset()
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

    open fun createPositionTracker(tentity: TEntity): EntityFrameStateTracker<TEntity> {
        return EntityFrameStateTracker(tentity)
    }

    open fun getPositionTracker(): EntityFrameStateTracker<TEntity> = positionTracker

    open fun getSeekTime(): Float = seekTime

    open fun addAnimationController(controller: IAnimationController<*>) {
        manager.addAnimationController(controller)
    }

    open fun getAnimationData(): AnimationData = manager

    open fun resolveExpression(str: String): IValue? = null

    open fun getAudioStreamFactory(str: String): Optional<IAudioStreamFactory> = Optional.empty()

    fun getAnimationExpressions(str: String): MutableList<IValue>? = animationMap?.get(str)

    open fun getPhysicsManager(): PhysicsManager = physicsManager

    open fun getAnimationEntries(str: String): AnimationController? = null

    open fun getTextureIndex(): Int = 0

    open fun getScale(): Float = 0.15f

    open fun setupAnim(seekTime: Float, z: Boolean) {}
    open fun afterSetupAnim(seekTime: Float, z: Boolean) {}

    fun getEntity(): TEntity = entity

    open fun hasCustomTexture(): Boolean = false

    open fun getBone(i: Int): IBone? = animationProcessor.getBone(i)

    open fun shouldRenderOverlay(): Boolean = true

    open fun getRefreshRate(): Int {
        val player = Minecraft.getInstance().player as? TEntity
        if (player != null && player != entity) {
            val vec3Position: Vec3 = player.position()
            if (vec3Position.x != 0.0 || vec3Position.y != 0.0 || vec3Position.z != 0.0) {
                if (!isFirstFrameAfterReset) {
                    return 10
                }
                val fDistanceTo: Float = player.distanceTo(entity)
                if (fDistanceTo > 64.0f) {
                    return 30
                }
                if (fDistanceTo > 40.0f) {
                    return 60
                }
            }
        }
        return ClientTickEvent.getRefreshRate()
    }

    fun processAnimation(partialTick: Float): AnimationEvent<*>? {
        return processAnimationImpl(partialTick, ModelPreviewRenderer.isFirstPersonOnRenderThread())
    }

    open fun processAnimationImpl(partialTick: Float, z: Boolean): AnimationEvent<*>? {
        if (currentModel == null) {
            return null
        }
        val entity: Entity = this.entity
        val livingEntity: LivingEntity? = entity as? LivingEntity
        val tickCount: Int = if (this is IPreviewAnimatable) ClientTickEvent.getTickCount() else entity.tickCount
        val frameTime: Float = if (partialTick != 1.0f) partialTick else Minecraft.getInstance().deltaTracker.getGameTimeDeltaPartialTick(false)
        val shouldSit: Boolean = entity.isPassenger && entity.vehicle != null && EntityDataBridge.shouldRiderSit(entity.vehicle)
        var limbSwingAmount = 0.0f
        var limbSwing = 0.0f
        if (!shouldSit && entity.isAlive && livingEntity != null) {
            limbSwingAmount = livingEntity.walkAnimation.speed(partialTick)
            limbSwing = livingEntity.walkAnimation.position(partialTick)
            if (livingEntity.isBaby) {
                limbSwing *= 3.0f
            }
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
            netHeadYaw = lerpHeadRot - lerpBodyRot
            val clampedHeadYaw: Float = Mth.clamp(Mth.wrapDegrees(lerpHeadRot - lerpBodyRot), -85.0f, 85.0f)
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
        val event: AnimationEvent<AnimatableEntity<TEntity>> = AnimationEvent(this, limbSwing, limbSwingAmount, tickCount, partialTick, frameTime, limbSwingAmount <= (-getScale()) || limbSwingAmount <= getScale(), z, modelData)
        val context: AnimationContext<TEntity> = AnimationContext(entity, this, event, modelData)
        getLogger()?.let { context.setLogger(it) }
        setCustomAnimations(context, event)
        return event
    }

    open fun setCustomAnimations(ctx: AnimationContext<*>, event: AnimationEvent<AnimatableEntity<TEntity>>) {
        var currentTick: Float = event.currentTick
        val z: Boolean = !shouldSkipAnimation(event)
        if (currentTick > lastTick) {
            hasUpdatedThisTick = false
            isTickTriggered = false
            lastTick = currentTick
            rateLimiter.setRefreshRate(getRefreshRate())
            isFirstFrameAfterReset = needsReset
            needsReset = false
        } else {
            currentTick = lastTick
        }
        if (manager.startTick == -1.0f) {
            manager.startTick = currentTick
        } else {
            val f2: Float = currentTick - manager.startTick
            val f3: Float = f2 - manager.limbSwing
            if (f3 > 0.0f) {
                manager.limbSwing = f2
                seekTime += f3
            }
        }
        event.currentTick = seekTime
        if (!animationProcessor.isDisabled()) {
            isTickTriggered = isTickTriggered or rateLimiter.request(seekTime / 20.0f)
            val z2: Boolean = (isTickTriggered && !hasUpdatedThisTick) || wasAnimationActiveLastTick || z
            val z3: Boolean = (!z || (seekTime == 0.0f && !hasUpdatedThisTick)) && isTickTriggered && !hasUpdatedThisTick
            resetHeadTracking(wasEvaluatedLastFrame)
            if (z2) {
                if (z3) {
                    hasUpdatedThisTick = true
                    positionTracker.updateState(event.getTickCount(), seekTime, event.getFrameTime())
                }
                getPhysicsManager().update(seekTime)
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
    open fun resetHeadTracking(z: Boolean) {}

    open fun getEvaluationContext(): AnimationProcessor<TEntity> = animationProcessor

    open fun initAnimationControllers(model: GeoModel, object2ReferenceMap: Object2ReferenceMap<String, MutableList<IValue>>) {
        reset()
        val animModel = AnimatedGeoModel(model)
        currentModel = animModel
        animationMap = object2ReferenceMap
        registerAnimationControllers()
        animationProcessor.initBones(animModel, object2ReferenceMap)
        setCurrentModel(animModel)
    }

    open fun clearAnimationControllers() {
        currentModel?.let {
            val model: GeoModel = it.getGeoModel()
            val object2ReferenceMap: Object2ReferenceMap<String, MutableList<IValue>>? = animationMap
            reset()
            initAnimationControllers(model, object2ReferenceMap ?: return)
        }
    }

    fun getCurrentModel(): AnimatedGeoModel? = currentModel

    open fun setCurrentModel(model: AnimatedGeoModel?) {}

    open fun shouldSkipAnimation(event: AnimationEvent<*>): Boolean = true

    open fun resetAnimationState() {
        needsReset = true
    }

    open fun executeExpression(value: IValue, isClientPlayer: Boolean, executeBeforeAnimation: Boolean, consumer: Consumer<String>?) {
        consumer?.let {
            animationProcessor.execute(value, isClientPlayer, executeBeforeAnimation, it)
        } ?: animationProcessor.execute(value, isClientPlayer, executeBeforeAnimation, null)
    }

    open fun getPropertyGetter(): IForeignVariableStorage = animationProcessor.getPublicVariableStorage()

    open fun markModelInitialized() {
        modelInitialized = true
    }

    open fun isModelInitialized(): Boolean = modelInitialized

    open fun getLogger(): ILogger? = null

    open fun isDebugMode(): Boolean {
        return Minecraft.getInstance().level == entity.level() && !entity.isRemoved
    }

    open fun setAnimationState(name: String, state: AnimationState) {
        animationStates[name] = state
    }

    open fun getAnimationState(name: String): AnimationState {
        return animationStates.getOrDefault(name, AnimationState.IDLE)
    }

    fun interface AnimationControllerVisitor : Consumer<Consumer<IAnimationController<*>>>
}