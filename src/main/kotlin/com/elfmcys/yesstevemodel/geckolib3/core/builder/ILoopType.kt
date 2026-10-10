package com.elfmcys.yesstevemodel.geckolib3.core.builder

import com.google.gson.JsonElement
import java.util.*

interface ILoopType {
    fun isRepeatingAfterEnd(): Boolean

    companion object {
        @JvmStatic
        fun fromJson(json: JsonElement?): ILoopType {
            if (json == null || !json.isJsonPrimitive) {
                return EDefaultLoopTypes.PLAY_ONCE
            }
            val primitive = json.asJsonPrimitive
            if (primitive.isBoolean) {
                return if (primitive.asBoolean) EDefaultLoopTypes.LOOP else EDefaultLoopTypes.PLAY_ONCE
            }
            if (primitive.isString) {
                val string = primitive.asString
                if ("false".equals(string, ignoreCase = true)) {
                    return EDefaultLoopTypes.PLAY_ONCE
                }
                if ("true".equals(string, ignoreCase = true)) {
                    return EDefaultLoopTypes.LOOP
                }
                runCatching {
                    return EDefaultLoopTypes.valueOf(string.uppercase(Locale.ROOT))
                }
            }
            return EDefaultLoopTypes.PLAY_ONCE
        }
    }

    enum class EDefaultLoopTypes(private val looping: Boolean = false) : ILoopType {
        LOOP(true),
        PLAY_ONCE(),
        HOLD_ON_LAST_FRAME();

        override fun isRepeatingAfterEnd(): Boolean {
            return looping
        }
    }
}