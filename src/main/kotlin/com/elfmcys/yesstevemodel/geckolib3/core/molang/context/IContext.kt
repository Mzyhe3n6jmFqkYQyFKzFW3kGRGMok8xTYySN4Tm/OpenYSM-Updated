package com.elfmcys.yesstevemodel.geckolib3.core.molang.context

import com.elfmcys.yesstevemodel.audio.AudioPlayerManager
import com.elfmcys.yesstevemodel.audio.PlaybackFlags
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationControllerContext
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent
import com.elfmcys.yesstevemodel.geckolib3.core.molang.storage.IControllerVariableStorage
import com.elfmcys.yesstevemodel.geckolib3.core.molang.storage.IForeignVariableStorage
import com.elfmcys.yesstevemodel.geckolib3.core.molang.storage.IScopedVariableStorage
import com.elfmcys.yesstevemodel.geckolib3.core.molang.storage.ITempVariableStorage
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.geckolib3.model.provider.data.EntityModelData
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.Function
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.network.chat.Component
import net.minecraft.util.RandomSource

interface IContext<TEntity> {
    val entity: TEntity
    val geoInstance: AnimatableEntity<*>
    val mc: Minecraft
    val level: ClientLevel?
    val animationEvent: AnimationEvent<*>
    val data: EntityModelData
    val animationControllerContext: AnimationControllerContext?
    val playbackFlags: PlaybackFlags?
    fun random(): RandomSource?
    fun <TChild> createChild(child: TChild): IContext<TChild>
    fun tempStorage(): ITempVariableStorage?
    fun scopedStorage(): IScopedVariableStorage?
    fun controllerStorage(): IControllerVariableStorage?
    fun foreignStorage(): IForeignVariableStorage?
    fun resolveExpression(str: String): IValue?
    fun callFunction(context: ExecutionContext<*>, value: IValue, list: List<*>): Any?
    fun callFunctionWithArgs(context: ExecutionContext<*>, value: IValue, arguments: Function.ArgumentCollection): Any?
    fun getAnimationLayers(): List<*>?
    val isDebugMode: Boolean
    val isClientSide: Boolean
    fun logWarning(str: String, vararg objArr: Any)
    fun logWarningComponent(component: Component)
    fun getAudioPlayerManager(global: Boolean): AudioPlayerManager?
}