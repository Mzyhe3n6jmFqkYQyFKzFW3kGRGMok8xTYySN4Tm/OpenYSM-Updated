package com.elfmcys.yesstevemodel.config

import net.minecraftforge.common.ForgeConfigSpec

object LoadingStateConfig {
    enum class Position {
        TOP_LEFT,
        TOP_CENTER,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_CENTER,
        BOTTOM_RIGHT
    }

    lateinit var DISABLE_LOADING_STATE_SCREEN: ForgeConfigSpec.BooleanValue
    lateinit var LOADING_STATE_POSITION: ForgeConfigSpec.EnumValue<Position>

    fun save() = GeneralConfig.save()

    fun define(builder: ForgeConfigSpec.Builder) {
        builder.push("loading_state_screen")
        builder.comment("Whether to disable loading state screen")
        DISABLE_LOADING_STATE_SCREEN = builder.define("DisableLoadingStateScreen", false)
        builder.comment("Loading state screen position")
        LOADING_STATE_POSITION = builder.defineEnum("LoadingStatePosition", Position.TOP_CENTER)
        builder.pop()
    }
}
