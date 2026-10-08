package com.elfmcys.yesstevemodel.geckolib3.core.controller

import com.elfmcys.yesstevemodel.audio.AudioPlayerManager
import com.elfmcys.yesstevemodel.client.entity.GeoEntity
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext
import com.elfmcys.yesstevemodel.geckolib3.core.molang.storage.IControllerVariableStorage
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator
import com.elfmcys.yesstevemodel.molang.runtime.Function
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ReferenceArrayList

open class AnimationControllerContext : IControllerVariableStorage {
    private var audioPlayerManager: AudioPlayerManager? = null
    private var animTime: Float = 0.0f
    private var propertyMap: Int2ObjectOpenHashMap<Any>? = null
    private var captureCount: Int = 0
    private var capturedArgs: ReferenceArrayList<ReferenceArrayList<Any>>? = null

    open fun setAnimTime(animTime: Float) {
        this.animTime = animTime
    }

    open fun animTime(): Float = animTime

    open fun getAudioPlayerManager(): AudioPlayerManager {
        val manager = audioPlayerManager ?: AudioPlayerManager().also { audioPlayerManager = it }
        return manager
    }

    override fun getControllerVariable(address: Int): Any? {
        return propertyMap?.get(address)
    }

    override fun setControllerVariable(address: Int, value: Any?) {
        if (value == null) {
            propertyMap?.remove(address)
            return
        }
        val map = propertyMap ?: Int2ObjectOpenHashMap<Any>().also { propertyMap = it }
        map.put(address, value)
    }

    open fun captureArguments(context: ExecutionContext<*>, controllerAddress: Int, arguments: Function.ArgumentCollection, startIndex: Int) {
        val argsList = capturedArgs ?: ReferenceArrayList<ReferenceArrayList<Any>>().also { capturedArgs = it }
        val captureIndex = captureCount++
        val capturedFrame: ReferenceArrayList<Any>
        if (argsList.size <= captureIndex) {
            capturedFrame = ReferenceArrayList(arguments.size() - startIndex)
            argsList.add(capturedFrame)
        } else {
            capturedFrame = argsList[captureIndex]
        }
        capturedFrame.size(arguments.size() - startIndex)
        for (argIndex in startIndex until arguments.size()) {
            capturedFrame[argIndex - startIndex] = arguments.getValue(context, argIndex)
        }
    }

    open fun executeRenderLayers(evaluator: ExpressionEvaluator<AnimationContext<*>>) {
        if (captureCount > 0) {
            val context: AnimationContext<*> = evaluator.entity()
            val animatableEntity: AnimatableEntity<*> = context.geoInstance()
            if (animatableEntity is GeoEntity) {
                val values: List<IValue>? = animatableEntity.renderLayers
                if (values != null) {
                    context.setIsClientSide(true)
                    val argsList = capturedArgs
                    if (argsList != null) {
                        for (i in captureCount - 1 downTo 0) {
                            for (value in values) {
                                context.callFunction(evaluator, value, argsList[i])
                            }
                        }
                    }
                    context.setIsClientSide(false)
                }
            }
            captureCount = 0
        }
        propertyMap?.clear()
    }
}