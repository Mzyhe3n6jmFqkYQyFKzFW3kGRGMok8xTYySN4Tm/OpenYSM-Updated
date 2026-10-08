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
    override val entity: TEntity,
    private val instance: AnimatableEntity<*>,
    override val animationEvent: AnimationEvent<*>,
    override val data: EntityModelData
) : IContext<TEntity> {
    private var animationControllerContext2: AnimationControllerContext? = null
    override var playbackFlags: PlaybackFlags? = null
    var audioPlayerManager: AudioPlayerManager? = null
    override var random: RandomSource? = null
    private var storage2: VariableStorage? = null
    var storage: VariableStorage?
        get() = storage2
        set(value) {
            storage2 = value
            foreignStorage = value
        }
    override var foreignStorage: IForeignVariableStorage? = null
        private set
    private var logger: ILogger? = null
    override var isClientSide: Boolean = false
        private set

    constructor(entity: TEntity, context: AnimationContext<*>) : this(
        entity,
        context.instance,
        context.animationEvent,
        context.data
    ) {
        animationControllerContext2 = context.animationControllerContext2
        random = context.random
        storage2 = context.storage2
        audioPlayerManager = context.audioPlayerManager
        when (entity) {
            is Player -> PlayerCapability[entity]?.let { cap -> foreignStorage = cap.propertyGetter }
            is Projectile -> ProjectileCapability[entity]?.let { cap -> foreignStorage = cap.propertyGetter }
            is Entity ->
                VehicleCapability[entity]?.let { cap -> foreignStorage = cap.propertyGetter }
        }
    }

    override val geoInstance: AnimatableEntity<*>
        get() = instance
    override val animationControllerContext: AnimationControllerContext?
        get() = animationControllerContext2
    override val mc: Minecraft
        get() = Minecraft.getInstance()
    override val level: ClientLevel?
        get() = mc.level

    override fun <TChild> createChild(child: TChild): IContext<TChild> {
        return AnimationContext(child, this)
    }

    override val tempStorage: ITempVariableStorage?
        get() = storage2?.localVariables
    override val scopedStorage: IScopedVariableStorage?
        get() = storage2
    override val controllerStorage: IControllerVariableStorage?
        get() = animationControllerContext

    override fun resolveExpression(str: String): IValue? = instance.resolveExpression(str)

    override fun callFunction(context: ExecutionContext<*>, value: IValue, list: List<*>): Any? {
        val localStorage = storage2?.localVariables ?: return null
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
        val localStorage = storage2?.localVariables ?: return null
        if (localStorage.pushScopeWithArgs(context, arguments)) {
            try {
                return value.evalSafe(context as ExpressionEvaluator)
            } finally {
                localStorage.popScope()
            }
        }
        return null
    }

    override val animationLayers: List<*>?
        get() = storage2?.localVariables?.asList()

    override val isDebugMode: Boolean
        get() = logger != null

    fun setIsClientSide(z: Boolean) {
        isClientSide = z
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
            val audioPlayerManager2 = animationControllerContext?.audioPlayerManager
            if (audioPlayerManager2 != null) return audioPlayerManager2
            val audioPlayerManager1 = playbackFlags?.audioPlayerManager
            if (audioPlayerManager1 != null) return audioPlayerManager1
        }
        return audioPlayerManager
    }

    fun setAnimationControllerContext(context: AnimationControllerContext?) {
        animationControllerContext2 = context
    }

    fun setLogger(logger: ILogger?) {
        this.logger = logger
    }
}