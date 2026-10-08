@file:Suppress("unused")

package com.elfmcys.yesstevemodel.geckolib3.util.json

import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType
import com.elfmcys.yesstevemodel.geckolib3.core.event.ParticleEventKeyFrame
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.BoneAnimation
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.BoneKeyFrame
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.BoneKeyFrameProcessor
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.RawBoneKeyFrame
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.event.EventKeyFrame
import com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.geckolib3.util.AnimationUtils
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Lists
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import net.minecraft.server.ChainedJsonException
import java.util.*
import kotlin.math.max

object JsonAnimationUtils {
    @JvmStatic
    @Throws(ClassCastException::class, IllegalStateException::class)
    fun deserializeJsonToAnimation(
        element: Map.Entry<String, JsonElement>,
        parser: MolangParser,
        mergeMultilineExpr: Boolean
    ): Animation {
        val animationJsonObject = element.value.asJsonObject
        val animationName = element.key
        val animationLength = animationJsonObject.get("animation_length")
        var animationLengthTicks =
            if (animationLength == null) -1.0f else AnimationUtils.convertSecondsToTicks(animationLength.asFloat)

        val loop = ILoopType.fromJson(animationJsonObject.get("loop"))

        var blendWeight: IValue? = null
        if (animationJsonObject.has("blend_weight")) {
            blendWeight = parser.parseExpression(animationJsonObject.get("blend_weight").asString, false)
        }

        var overridePrevAnim: Boolean? = null
        if (animationJsonObject.has("override_previous_animation")) {
            overridePrevAnim = animationJsonObject.get("override_previous_animation").asBoolean
        }

        val boneAnimations = ReferenceArrayList<BoneAnimation>()
        val customInstructionKeyframes = ReferenceArrayList<EventKeyFrame<Array<IValue>>>()
        val soundKeyFrames = ReferenceArrayList<EventKeyFrame<String>>()

        for ((key, value) in animationJsonObject.soundEffects) {
            val startTick = (key.toDoubleOrNull() ?: 0.0) * 20.0

            when {
                value.isJsonPrimitive && value.asJsonPrimitive.isString -> {
                    soundKeyFrames.add(EventKeyFrame(startTick, value.asString))
                }

                value.isJsonObject -> {
                    val effectObj = value.asJsonObject
                    if (effectObj.has("effect")) {
                        soundKeyFrames.add(EventKeyFrame(startTick, effectObj.get("effect").asString))
                    }
                }
            }
        }

        for ((key, value) in animationJsonObject.customInstructionKeyFrames) {
            val startTick = (key.toDoubleOrNull() ?: 0.0) * 20.0
            when {
                value.isJsonArray -> {
                    val array: JsonArray = value.asJsonArray
                    val values: Array<IValue> = JsonMolangUtils.getExpressions(array, parser, mergeMultilineExpr)
                    customInstructionKeyframes.add(EventKeyFrame(startTick, values))
                }

                value.isJsonPrimitive && value.asJsonPrimitive.isString -> {
                    val values: Array<IValue> = arrayOf(parser.parseExpression(value.asString, false))
                    customInstructionKeyframes.add(EventKeyFrame(startTick, values))
                }
            }
        }

        customInstructionKeyframes.sortWith(Comparator.comparingDouble { it.startTick.toDouble() })

        for ((key, value) in animationJsonObject.bones) {
            val rotationKeyFrames: MutableList<RawBoneKeyFrame> = Lists.newArrayList()
            val positionKeyFrames: MutableList<RawBoneKeyFrame> = Lists.newArrayList()
            val scaleKeyFrames: MutableList<RawBoneKeyFrame> = Lists.newArrayList()
            val boneJsonObj = value.asJsonObject

            JsonKeyFrameUtils.getKeyFrames(scaleKeyFrames, boneJsonObj.get("scale"), parser)
            JsonKeyFrameUtils.getKeyFrames(positionKeyFrames, boneJsonObj.get("position"), parser)
            JsonKeyFrameUtils.getKeyFrames(rotationKeyFrames, boneJsonObj.get("rotation"), parser)

            boneAnimations.add(
                BoneAnimation(
                    key,
                    BoneKeyFrameProcessor.process(rotationKeyFrames, true),
                    BoneKeyFrameProcessor.process(positionKeyFrames, false),
                    BoneKeyFrameProcessor.process(scaleKeyFrames, false)
                )
            )
        }

        if (animationLengthTicks == -1.0f) {
            animationLengthTicks = calculateLength(boneAnimations)
        }

        return Animation(
            animationName,
            animationLengthTicks.toDouble(),
            loop,
            blendWeight = blendWeight,
            override = overridePrevAnim,
            boneAnimations = boneAnimations.toTypedArray(),
            soundKeyFrames = soundKeyFrames.toTypedArray(),
            particleKeyFrames = emptyArray<ParticleEventKeyFrame>(),
            customInstructionKeyframes = customInstructionKeyframes.toTypedArray()
        )
    }

    private fun calculateLength(boneAnimations: List<BoneAnimation>): Float {
        var longestLength = 0.0f
        for (animation in boneAnimations) {
            val xKeyframeTime = calculateKeyFrameListLength(animation.rotationKeyFrames)
            val yKeyframeTime = calculateKeyFrameListLength(animation.positionKeyFrames)
            val zKeyframeTime = calculateKeyFrameListLength(animation.scaleKeyFrames)
            longestLength = maxAll(longestLength, xKeyframeTime, yKeyframeTime, zKeyframeTime)
        }
        return if (longestLength == 0.0f) Float.MAX_VALUE else longestLength
    }

    private fun calculateKeyFrameListLength(boneKeyFrames: List<BoneKeyFrame>): Float {
        if (boneKeyFrames.isEmpty()) return 0.0f
        return boneKeyFrames[boneKeyFrames.size - 1].startTick
    }

    @JvmStatic
    fun maxAll(vararg values: Float): Float {
        var max = 0.0f
        for (value in values) max = max(value, max)
        return max
    }
}

@Throws(ChainedJsonException::class)
private fun getObjectByKey(json: Set<Map.Entry<String, JsonElement>>, key: String): JsonElement {
    for ((key1, value) in json) {
        if (key1 == key) {
            return value
        }
    }
    throw ChainedJsonException("Could not find key: $key")
}

@Throws(ChainedJsonException::class)
fun JsonObject.getAnimation(animationName: String): Map.Entry<String, JsonElement> =
    AbstractMap.SimpleEntry(animationName, getObjectByKey(animations, animationName))

val JsonObject.customInstructionKeyFrames: List<Map.Entry<String, JsonElement>>
    get() {
        val customInstructions = getAsJsonObject("timeline")
        return customInstructions?.entrySet()?.toList() ?: emptyList()
    }

val JsonObject.soundEffects: List<Map.Entry<String, JsonElement>>
    get() {
        val bones = getAsJsonObject("sound_effects")
        return bones?.entrySet()?.toList() ?: emptyList()
    }

val JsonObject.bones: List<Map.Entry<String, JsonElement>>
    get() {
        val bones = getAsJsonObject("bones")
        return bones?.entrySet()?.toList() ?: emptyList()
    }

val JsonObject.animations: Set<Map.Entry<String, JsonElement>>
    get() {
        if (has("animations")) return getAsJsonObject("animations").entrySet()
        return ImmutableSet.of()
    }