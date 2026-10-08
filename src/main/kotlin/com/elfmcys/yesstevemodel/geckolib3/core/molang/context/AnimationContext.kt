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

class AnimationContext<TEntity>(
    private val entity2: TEntity,
    val instance: AnimatableEntity<*>,
    private val animationEvent2: AnimationEvent<*>,
    private val data2: EntityModelData
) : IContext<TEntity> {
    private var animationControllerContext2: AnimationControllerContext? = null
    private var playbackFlags2: PlaybackFlags? = null
    var audioPlayerManager: AudioPlayerManager? = null
    var random: RandomSource? = null
    var storage: VariableStorage? = null
    var foreignStorage: IForeignVariableStorage? = null
    var logger: ILogger? = null
    private var isClientSide2: Boolean = false

    constructor(entity: TEntity, context: AnimationContext<*>) : this(
        entity,
        context.instance,
        context.animationEvent2,
        context.data2
    ) {
        animationControllerContext2 = context.animationControllerContext2
        random = context.random
        storage = context.storage
        audioPlayerManager = context.audioPlayerManager
        when (entity) {
            is Player -> PlayerCapability[entity]?.let { cap -> foreignStorage = cap.propertyGetter }
            is Projectile -> ProjectileCapability[entity]?.let { cap -> foreignStorage = cap.propertyGetter }
            is Entity ->
                VehicleCapability[entity]?.let { cap -> foreignStorage = cap.propertyGetter }
        }
    }

    override val animationEvent: AnimationEvent<*>
        get() = animationEvent2
    override val geoInstance: AnimatableEntity<*>
        get() = instance

    override val data: EntityModelData
        get() = data2
    override var animationControllerContext: AnimationControllerContext?
        get() = animationControllerContext2
        set(value) {
            animationControllerContext2 = value
        }
    override var playbackFlags: PlaybackFlags?
        get() = playbackFlags2
        set(value) {
            playbackFlags2 = value
        }

    override fun random(): RandomSource? = random
    override val entity: TEntity
        get() = entity2
    override val mc: Minecraft
        get() = Minecraft.getInstance()
    override val level: ClientLevel?
        get() = mc.level

    override fun <TChild> createChild(child: TChild): IContext<TChild> {
        return AnimationContext(child, this)
    }

    override fun tempStorage(): ITempVariableStorage? = storage?.localVariables
    override fun scopedStorage(): IScopedVariableStorage? = storage
    override fun foreignStorage(): IForeignVariableStorage? = foreignStorage
    override fun controllerStorage(): IControllerVariableStorage? = animationControllerContext2

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

    override val isDebugMode: Boolean
        get() = logger != null
    override val isClientSide: Boolean
        get() = isClientSide2

    fun setIsClientSide(z: Boolean) {
        isClientSide2 = z
    }

    override fun logWarning(str: String, vararg objArr: Any) {
        if (isDebugMode) {
            logger?.logFormatted(str, *objArr)
        }
    }

    override fun logWarningComponent(component: Component) {
        if (isDebugMode) {
            logger?.logComponent(component)
        }
    }

    override fun getAudioPlayerManager(global: Boolean): AudioPlayerManager? {
        if (!global) {
            val audioPlayerManager2 = animationControllerContext2?.audioPlayerManager
            if (audioPlayerManager2 != null) {
                return audioPlayerManager2
            }
            val audioPlayerManager1 = playbackFlags2?.audioPlayerManager
            if (audioPlayerManager1 != null) {
                return audioPlayerManager1
            }
        }
        return audioPlayerManager
    }

    fun setAudioPlayerManager(audioPlayerManager: AudioPlayerManager?) {
        this.audioPlayerManager = audioPlayerManager
    }

    fun setAnimationControllerContext(context: AnimationControllerContext?) {
        animationControllerContext2 = context
    }

    fun setStorage(variableStorage: VariableStorage?) {
        storage = variableStorage
        foreignStorage = variableStorage
    }

    fun setRandom(random: RandomSource?) {
        this.random = random
    }

    fun setLogger(logger: ILogger?) {
        this.logger = logger
    }
}