@file:Suppress("MemberVisibilityCanBePrivate")

package com.elfmcys.yesstevemodel.geckolib3.core.molang.context

import com.elfmcys.yesstevemodel.audio.AudioPlayerManager
import com.elfmcys.yesstevemodel.audio.PlaybackFlags
import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.capability.ProjectileCapability
import com.elfmcys.yesstevemodel.capability.VehicleCapability
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationControllerContext
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.core.molang.storage.*
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.geckolib3.model.provider.data.EntityModelData
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import com.elfmcys.yesstevemodel.molang.runtime.Function
import com.elfmcys.yesstevemodel.util.log.ILogger
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.network.chat.Component
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.Projectile

open class AnimationContext<TEntity>(
    @JvmField val entity: TEntity,
    @JvmField val instance: AnimatableEntity<*>,
    @JvmField val animationEvent: AnimationEvent<*>,
    @JvmField val data: EntityModelData
) : IContext<TEntity> {
    @JvmField var animationControllerContext: AnimationControllerContext? = null
    @JvmField var playbackFlags: PlaybackFlags? = null
    @JvmField var audioPlayerManager: AudioPlayerManager? = null
    @JvmField var random: RandomSource? = null
    @JvmField var storage: VariableStorage? = null
    @JvmField var foreignStorage: IForeignVariableStorage? = null
    @JvmField var logger: ILogger? = null
    @JvmField var isClientSide: Boolean = false

    constructor(entity: TEntity, context: AnimationContext<*>) : this(
        entity,
        context.instance,
        context.animationEvent,
        context.data
    ) {
        animationControllerContext = context.animationControllerContext
        random = context.random
        storage = context.storage
        audioPlayerManager = context.audioPlayerManager
        when (entity) {
            is Player -> PlayerCapability[entity]?.let { cap -> foreignStorage = cap.getPropertyGetter() }
            is Projectile -> ProjectileCapability[entity]?.let { cap -> foreignStorage = cap.getPropertyGetter() }
            is Entity ->
                VehicleCapability[entity]?.let { cap -> foreignStorage = cap.getPropertyGetter() }
        }
    }

    override fun animationEvent(): AnimationEvent<*> = animationEvent
    override fun geoInstance(): AnimatableEntity<*> = instance
    override fun data(): EntityModelData = data
    override fun animationControllerContext(): AnimationControllerContext? = animationControllerContext
    override fun getPlaybackFlags(): PlaybackFlags? = playbackFlags
    override fun random(): RandomSource? = random
    override fun entity(): TEntity = entity
    override fun mc(): Minecraft = Minecraft.getInstance()
    override fun level(): ClientLevel? = mc().level

    override fun <TChild> createChild(child: TChild): IContext<TChild> {
        return AnimationContext(child, this)
    }

    override fun tempStorage(): ITempVariableStorage? = storage?.localVariables
    override fun scopedStorage(): IScopedVariableStorage? = storage
    override fun foreignStorage(): IForeignVariableStorage? = foreignStorage
    override fun controllerStorage(): IControllerVariableStorage? = animationControllerContext

    override fun resolveExpression(str: String): IValue? = instance.resolveExpression(str)

    override fun callFunction(context: ExecutionContext<*>, value: IValue, list: List<*>): Any? {
        val localStorage = storage?.localVariables ?: return null
        if (localStorage.pushScope(list)) {
            try {
                return value.evalSafe(context as ExpressionEvaluator)
            } finally {
                localStorage.popScope()
            }
        }
        return null
    }

    override fun callFunctionWithArgs(
        context: ExecutionContext<*>,
        value: IValue,
        arguments: Function.ArgumentCollection
    ): Any? {
        val localStorage = storage?.localVariables ?: return null
        if (localStorage.pushScopeWithArgs(context, arguments)) {
            try {
                return value.evalSafe(context as ExpressionEvaluator)
            } finally {
                localStorage.popScope()
            }
        }
        return null
    }

    override fun getAnimationLayers(): List<*>? = storage?.localVariables?.asList()

    override fun isDebugMode(): Boolean = logger != null
    override fun isClientSide(): Boolean = isClientSide
    open fun setIsClientSide(z: Boolean) {
        isClientSide = z
    }

    override fun logWarning(str: String, vararg objArr: Any) {
        if (isDebugMode()) {
            logger?.logFormatted(str, *objArr)
        }
    }

    override fun logWarningComponent(component: Component) {
        if (isDebugMode()) {
            logger?.logComponent(component)
        }
    }

    override fun getAudioPlayerManager(global: Boolean): AudioPlayerManager? {
        if (!global) {
            val audioPlayerManager2 = animationControllerContext?.getAudioPlayerManager()
            if (audioPlayerManager2 != null) {
                return audioPlayerManager2
            }
            val audioPlayerManager1 = playbackFlags?.audioPlayerManager
            if (audioPlayerManager1 != null) {
                return audioPlayerManager1
            }
        }
        return audioPlayerManager
    }

    open fun setAudioPlayerManager(audioPlayerManager: AudioPlayerManager?) {
        this.audioPlayerManager = audioPlayerManager
    }

    open fun setAnimationControllerContext(context: AnimationControllerContext?) {
        animationControllerContext = context
    }

    open fun setPlaybackFlags(playbackFlags2: PlaybackFlags?) {
        playbackFlags = playbackFlags2
    }

    open fun setStorage(variableStorage: VariableStorage?) {
        storage = variableStorage
        foreignStorage = variableStorage
    }

    open fun setRandom(random: RandomSource?) {
        this.random = random
    }

    open fun setLogger(logger: ILogger?) {
        this.logger = logger
    }
}