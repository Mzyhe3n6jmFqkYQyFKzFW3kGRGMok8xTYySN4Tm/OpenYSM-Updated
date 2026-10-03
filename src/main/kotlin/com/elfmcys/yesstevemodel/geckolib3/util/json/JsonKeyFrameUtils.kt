package com.elfmcys.yesstevemodel.geckolib3.util.json

import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.EasingType
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.RawBoneKeyFrame
import com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue
import com.elfmcys.yesstevemodel.geckolib3.util.AnimationUtils.convertSecondsToTicks
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.util.Locale

/**
 * 用于将 json 转换成关键帧的工具类
 */
object JsonKeyFrameUtils {
    @JvmStatic
    @Throws(NumberFormatException::class)
    fun getKeyFrames(boneKeyFrames: MutableList<RawBoneKeyFrame>, element: JsonElement?, parser: MolangParser) {
        if (element == null) {
            return
        }

        if (element.isJsonPrimitive || element.isJsonArray) {
            val keyframe = RawBoneKeyFrame()
            keyframe.startTick = 0.0
            readPreKeyFrame(element, keyframe, parser)
            boneKeyFrames.add(keyframe)
            return
        }

        if (!element.isJsonObject) {
            return
        }

        val obj: JsonObject = element.asJsonObject
        for (time in obj.keySet()) {
            val keyframe = RawBoneKeyFrame()
            keyframe.startTick = convertSecondsToTicks(time.toFloat()).toDouble()

            val item: JsonElement = obj.get(time)
            if (item.isJsonPrimitive || item.isJsonArray) {
                readPreKeyFrame(item, keyframe, parser)
            } else if (item.isJsonObject) {
                val jsonObject = item.asJsonObject
                if (jsonObject.has("vector")) {
                    val vector = jsonObject.get("vector")
                    val easing = jsonObject.get("easing")
                    readPreKeyFrame(vector, keyframe, parser)
                    tryGetEasingType(easing, keyframe)
                } else {
                    val pre = jsonObject.get("pre")
                    val post = jsonObject.get("post")
                    val easing = jsonObject.get("lerp_mode")

                    if (pre != null && post != null) {
                        readPreKeyFrame(pre, keyframe, parser)
                        readPostKeyFrame(post, keyframe, parser)
                        keyframe.contiguous = false
                    } else {
                        if (pre != null) {
                            readPreKeyFrame(pre, keyframe, parser)
                        } else if (post != null) {
                            // 没错，post 赋给 pre
                            readPreKeyFrame(post, keyframe, parser)
                        }
                    }
                    tryGetEasingType(easing, keyframe)
                }
            }
            boneKeyFrames.add(keyframe)
        }

        // 排序
        boneKeyFrames.sortWith(Comparator.comparingDouble { it.startTick.toDouble() })
    }

    @JvmStatic
    fun tryGetEasingType(element: JsonElement?, keyframe: RawBoneKeyFrame) {
        if (element == null || !element.isJsonPrimitive || !element.asJsonPrimitive.isString) {
            return
        }
        val easingTypeText = element.asJsonPrimitive.asString.lowercase(Locale.ENGLISH)
        if ("linear" == easingTypeText) {
            keyframe.easingType = EasingType.LINEAR
        } else if ("catmullrom" == easingTypeText) {
            keyframe.easingType = EasingType.CATMULLROM
        }
    }

    @JvmStatic
    fun readPreKeyFrame(element: JsonElement, keyframe: RawBoneKeyFrame, parser: MolangParser) {
        if (element.isJsonPrimitive) {
            val primitive = element.asJsonPrimitive
            if (primitive.isString) {
                val value: IValue = parser.parseExpression(primitive.asString, false)
                keyframe.preXValue = value
                keyframe.preYValue = value
                keyframe.preZValue = value
            } else if (primitive.isNumber) {
                val value = primitive.asDouble
                keyframe.preX = value
                keyframe.preY = value
                keyframe.preZ = value
            }
            return
        }

        if (element.isJsonArray) {
            val array = element.asJsonArray
            if (array.isEmpty) {
                return
            }

            if (array.size() >= 3) {
                val xPri = array[0].asJsonPrimitive
                val yPri = array[1].asJsonPrimitive
                val zPri = array[2].asJsonPrimitive

                if (xPri.isString) {
                    keyframe.preXValue = parser.parseExpression(xPri.asString, false)
                } else if (xPri.isNumber) {
                    keyframe.preX = xPri.asDouble
                }

                if (yPri.isString) {
                    keyframe.preYValue = parser.parseExpression(yPri.asString, false)
                } else if (yPri.isNumber) {
                    keyframe.preY = yPri.asDouble
                }

                if (zPri.isString) {
                    keyframe.preZValue = parser.parseExpression(zPri.asString, false)
                } else if (zPri.isNumber) {
                    keyframe.preZ = zPri.asDouble
                }

                return
            }

            val primitive = array[0].asJsonPrimitive
            if (primitive.isString) {
                val value: IValue = parser.parseExpression(primitive.asString, false)
                keyframe.preXValue = value
                keyframe.preYValue = value
                keyframe.preZValue = value
            } else if (primitive.isNumber) {
                val value = primitive.asDouble
                keyframe.preX = value
                keyframe.preY = value
                keyframe.preZ = value
            }
        }
    }

    @JvmStatic
    fun readPostKeyFrame(element: JsonElement, keyframe: RawBoneKeyFrame, parser: MolangParser) {
        if (element.isJsonPrimitive) {
            val primitive = element.asJsonPrimitive
            if (primitive.isString) {
                val value: IValue = parser.parseExpression(primitive.asString, false)
                keyframe.postXValue = value
                keyframe.postYValue = value
                keyframe.postZValue = value
            } else if (primitive.isNumber) {
                val value = primitive.asDouble
                keyframe.postX = value
                keyframe.postY = value
                keyframe.postZ = value
            }
            return
        }

        if (element.isJsonArray) {
            val array = element.asJsonArray
            if (array.isEmpty) {
                return
            }

            if (array.size() >= 3) {
                val xPri = array[0].asJsonPrimitive
                val yPri = array[1].asJsonPrimitive
                val zPri = array[2].asJsonPrimitive

                if (xPri.isString) {
                    keyframe.postXValue = parser.parseExpression(xPri.asString, false)
                } else if (xPri.isNumber) {
                    keyframe.postX = xPri.asDouble
                }

                if (yPri.isString) {
                    keyframe.postYValue = parser.parseExpression(yPri.asString, false)
                } else if (yPri.isNumber) {
                    keyframe.postY = yPri.asDouble
                }

                if (zPri.isString) {
                    keyframe.postZValue = parser.parseExpression(zPri.asString, false)
                } else if (zPri.isNumber) {
                    keyframe.postZ = zPri.asDouble
                }

                return
            }

            val primitive = array[0].asJsonPrimitive
            if (primitive.isString) {
                val value: IValue = parser.parseExpression(primitive.asString, false)
                keyframe.postXValue = value
                keyframe.postYValue = value
                keyframe.postZValue = value
            } else if (primitive.isNumber) {
                val value = primitive.asDouble
                keyframe.postX = value
                keyframe.postY = value
                keyframe.postZ = value
            }
        }
    }
}