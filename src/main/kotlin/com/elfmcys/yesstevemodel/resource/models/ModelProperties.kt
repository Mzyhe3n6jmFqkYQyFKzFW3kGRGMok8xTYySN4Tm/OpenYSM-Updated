@file:Suppress("unused")

package com.elfmcys.yesstevemodel.resource.models

import com.elfmcys.yesstevemodel.client.gui.custom.ExtraAnimationButtons
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap
import com.elfmcys.yesstevemodel.util.data.StringMapPair
import com.google.common.collect.Maps

class ModelProperties(
    val heightScale: Float,
    val widthScale: Float,
    val defaultTexture: String,
    val previewAnimation: String,
    val extraAnimation: OrderedStringMap<String, String>,
    extraAnimationButtons: Array<ExtraAnimationButtons>,
    extraAnimationClassify: Array<StringMapPair>,
    @get:JvmName("isFree")
    val free: Boolean,
    @get:JvmName("isRenderLayersFirst")
    val renderLayersFirst: Boolean,
    @get:JvmName("isDisablePreviewRotation")
    val disablePreviewRotation: Boolean
) {
    val extraAnimationButtons: Map<String, ExtraAnimationButtons> = buildExtraAnimationButtonsMap(extraAnimationButtons)
    val extraAnimationClassify: Map<String, OrderedStringMap<String, String>> =
        buildExtraAnimationClassifyMap(extraAnimationClassify)

    companion object {
        @JvmStatic
        fun buildExtraAnimationButtonsMap(extraAnimationButtons: Array<ExtraAnimationButtons>): Map<String, ExtraAnimationButtons> {
            val map = Maps.newHashMap<String, ExtraAnimationButtons>()
            for (buttons in extraAnimationButtons) {
                map[buttons.id] = buttons
            }
            return map
        }

        @JvmStatic
        fun buildExtraAnimationClassifyMap(extraAnimationClassify: Array<StringMapPair>): Map<String, OrderedStringMap<String, String>> {
            val map = Maps.newHashMap<String, OrderedStringMap<String, String>>()
            for (classify in extraAnimationClassify) {
                map[classify.key] = classify.valueMap
            }
            return map
        }
    }
}
