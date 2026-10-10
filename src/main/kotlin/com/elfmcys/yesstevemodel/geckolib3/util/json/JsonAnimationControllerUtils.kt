@file:Suppress("unused")

package com.elfmcys.yesstevemodel.geckolib3.util.json

import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationState
import com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.geckolib3.util.IInterpolable
import com.elfmcys.yesstevemodel.geckolib3.util.LinearKeyframeInterpolator
import com.elfmcys.yesstevemodel.geckolib3.util.TicksInterpolator
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Lists
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import org.apache.commons.lang3.tuple.Pair

/**
 * 解析动画控制器
 */
object JsonAnimationControllerUtils {
    fun getAnimationControllers(json: JsonObject): Set<Map.Entry<String, JsonElement>> {
        if (json.has("animation_controllers")) {
            return json.getAsJsonObject("animation_controllers").entrySet()
        }
        return ImmutableSet.of()
    }

    fun getStates(json: JsonObject): List<Map.Entry<String, JsonElement>> {
        val states = json.getAsJsonObject("states")
        return states?.entrySet()?.toList() ?: emptyList()
    }

    fun getAnimations(json: JsonObject): List<JsonElement> {
        val animations = json.getAsJsonArray("animations")
        return animations?.asList() ?: emptyList()
    }

    @Throws(ClassCastException::class, IllegalStateException::class)
    fun deserializeJsonToAnimationController(
        element: Map.Entry<String, JsonElement>,
        parser: MolangParser,
        mergeMultilineExpr: Boolean
    ): AnimationController {
        val animCtrlJsonObject = element.value.asJsonObject

        var initialState = "default"
        if (animCtrlJsonObject.has("initial_state")) {
            initialState = animCtrlJsonObject.get("initial_state").asString
        }

        val states = ReferenceArrayList<AnimationState>()
        for ((name, value) in getStates(animCtrlJsonObject)) {
            val stateJsonObject = value.asJsonObject

            val animations: MutableList<Pair<String, IValue>> = Lists.newArrayList()
            val transitions: MutableList<Pair<String, IValue>> = Lists.newArrayList()
            val soundEffects: MutableList<String> = Lists.newArrayList()

            getAnimations(animations, stateJsonObject.get("animations"), parser)
            getTransitions(transitions, stateJsonObject.get("transitions"), parser)
            getSoundEffects(soundEffects, stateJsonObject.get("sound_effects"))

            val onEntry = JsonMolangUtils.getExpressions(stateJsonObject.get("on_entry"), parser, mergeMultilineExpr)
            val onExit = JsonMolangUtils.getExpressions(stateJsonObject.get("on_exit"), parser, mergeMultilineExpr)

            val blendTransition = getBlendTransition(stateJsonObject.get("blend_transition"))

            var blendViaShortestPath = false
            if (stateJsonObject.has("blend_via_shortest_path")) {
                blendViaShortestPath = stateJsonObject.get("blend_via_shortest_path").asBoolean
            }

            states.add(
                AnimationState(
                    name,
                    animations.toTypedArray(),
                    transitions.toTypedArray(),
                    soundEffects.toTypedArray(),
                    onEntry,
                    onExit,
                    blendTransition,
                    blendViaShortestPath
                )
            )
        }

        return AnimationController(initialState, states.toTypedArray())
    }

    fun getAnimations(animations: MutableList<Pair<String, IValue>>, element: JsonElement?, parser: MolangParser) {
        if (element == null || !element.isJsonArray) return
        for (animation in element.asJsonArray) {
            when {
                animation.isJsonPrimitive -> {
                    animations.add(
                        Pair.of(
                            animation.asString,
                            com.elfmcys.yesstevemodel.geckolib3.core.molang.value.FloatValue.ZERO
                        )
                    )
                }

                animation.isJsonObject -> {
                    for ((key, value) in animation.asJsonObject.entrySet()) {
                        animations.add(Pair.of(key, parser.parseExpression(value.asString, false)))
                    }
                }
            }
        }
    }

    fun getTransitions(transitions: MutableList<Pair<String, IValue>>, element: JsonElement?, parser: MolangParser) {
        if (element == null || !element.isJsonArray) return
        for (transition in element.asJsonArray) {
            if (transition.isJsonObject) {
                for ((key, value) in transition.asJsonObject.entrySet()) {
                    transitions.add(Pair.of(key, parser.parseExpression(value.asString, false)))
                }
            }
        }
    }

    fun getSoundEffects(soundEffects: MutableList<String>, element: JsonElement?) {
        if (element == null || !element.isJsonArray) return
        for (soundEffect in element.asJsonArray) {
            when {
                soundEffect.isJsonObject -> {
                    val soundEffectObj = soundEffect.asJsonObject
                    if (soundEffectObj.has("effect")) {
                        soundEffects.add(soundEffectObj.get("effect").asString)
                    }
                }

                soundEffect.isJsonPrimitive -> {
                    soundEffects.add(soundEffect.asString)
                }
            }
        }
    }

    fun getBlendTransition(element: JsonElement?): IInterpolable {
        if (element == null) return TicksInterpolator(0.0f)
        when {
            element.isJsonPrimitive && element.asJsonPrimitive.isNumber -> {
                return TicksInterpolator(element.asFloat)
            }

            element.isJsonObject -> {
                val blendJsonObj = element.asJsonObject
                val sortedBlend = blendJsonObj.entrySet().sortedBy { e -> e.key.toDoubleOrNull() ?: 0.0 }
                val keys = FloatArray(sortedBlend.size)
                val values = FloatArray(sortedBlend.size)
                for ((i, blendEntry) in sortedBlend.withIndex()) {
                    keys[i] = blendEntry.key.toFloatOrNull() ?: 0.0f
                    values[i] = blendEntry.value.asFloat
                }
                return LinearKeyframeInterpolator(keys, values)
            }

            else -> return TicksInterpolator(0.0f)
        }
    }
}