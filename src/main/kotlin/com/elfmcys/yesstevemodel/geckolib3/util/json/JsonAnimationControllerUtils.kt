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
    @JvmStatic
    fun getAnimationControllers(json: JsonObject): Set<Map.Entry<String, JsonElement>> {
        if (json.has("animation_controllers")) {
            return json.getAsJsonObject("animation_controllers").entrySet()
        }
        return ImmutableSet.of()
    }

    @JvmStatic
    fun getStates(json: JsonObject): List<Map.Entry<String, JsonElement>> {
        val states = json.getAsJsonObject("states")
        return states?.entrySet()?.toList() ?: emptyList()
    }

    @JvmStatic
    fun getAnimations(json: JsonObject): List<JsonElement> {
        val animations = json.getAsJsonArray("animations")
        return animations?.asList() ?: emptyList()
    }

    @JvmStatic
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
        for (state in getStates(animCtrlJsonObject)) {
            val name = state.key
            val stateJsonObject = state.value.asJsonObject

            val animations: MutableList<Pair<String, IValue?>> = Lists.newArrayList()
            val transitions: MutableList<Pair<String, IValue>> = Lists.newArrayList()
            val soundEffects: MutableList<String> = Lists.newArrayList()

            getAnimations(animations, stateJsonObject.get("animations"), parser)
            getTransitions(transitions, stateJsonObject.get("transitions"), parser)
            getSoundEffects(soundEffects, stateJsonObject.get("sound_effects"))

            val onEntry: Array<IValue> = JsonMolangUtils.getExpressions(stateJsonObject.get("on_entry"), parser, mergeMultilineExpr)
            val onExit: Array<IValue> = JsonMolangUtils.getExpressions(stateJsonObject.get("on_exit"), parser, mergeMultilineExpr)

            val blendTransition: IInterpolable = getBlendTransition(stateJsonObject.get("blend_transition"))

            var blendViaShortestPath = false
            if (stateJsonObject.has("blend_via_shortest_path")) {
                blendViaShortestPath = stateJsonObject.get("blend_via_shortest_path").asBoolean
            }

            @Suppress("UNCHECKED_CAST")
            states.add(
                AnimationState(
                    name,
                    animations.toTypedArray() as Array<Pair<String, IValue>>,
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

    @JvmStatic
    fun getAnimations(animations: MutableList<Pair<String, IValue?>>, element: JsonElement?, parser: MolangParser) {
        if (element == null || !element.isJsonArray) {
            return
        }
        for (animation in element.asJsonArray) {
            if (animation.isJsonPrimitive) {
                animations.add(Pair.of(animation.asString, null))
            } else if (animation.isJsonObject) {
                for (entry in animation.asJsonObject.entrySet()) {
                    animations.add(Pair.of(entry.key, parser.parseExpression(entry.value.asString, false)))
                }
            }
        }
    }

    @JvmStatic
    fun getTransitions(transitions: MutableList<Pair<String, IValue>>, element: JsonElement?, parser: MolangParser) {
        if (element == null || !element.isJsonArray) {
            return
        }
        for (transition in element.asJsonArray) {
            if (transition.isJsonObject) {
                for (entry in transition.asJsonObject.entrySet()) {
                    transitions.add(Pair.of(entry.key, parser.parseExpression(entry.value.asString, false)))
                }
            }
        }
    }

    @JvmStatic
    fun getSoundEffects(soundEffects: MutableList<String>, element: JsonElement?) {
        if (element == null || !element.isJsonArray) {
            return
        }
        for (soundEffect in element.asJsonArray) {
            if (soundEffect.isJsonObject) {
                val soundEffectObj = soundEffect.asJsonObject
                if (soundEffectObj.has("effect")) {
                    soundEffects.add(soundEffectObj.get("effect").asString)
                }
            } else if (soundEffect.isJsonPrimitive) {
                soundEffects.add(soundEffect.asString)
            }
        }
    }

    @JvmStatic
    fun getBlendTransition(element: JsonElement?): IInterpolable {
        if (element == null) {
            return TicksInterpolator(0.0f)
        }
        if (element.isJsonPrimitive && element.asJsonPrimitive.isNumber) {
            return TicksInterpolator(element.asFloat)
        } else if (element.isJsonObject) {
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
        return TicksInterpolator(0.0f)
    }
}