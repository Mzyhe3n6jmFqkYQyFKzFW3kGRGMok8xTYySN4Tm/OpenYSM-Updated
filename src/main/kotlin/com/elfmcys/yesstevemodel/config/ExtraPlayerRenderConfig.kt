package com.elfmcys.yesstevemodel.config

import net.minecraftforge.common.ForgeConfigSpec

// TODO: Transparency support
object ExtraPlayerRenderConfig {
    lateinit var DISABLE_PLAYER_RENDER: ForgeConfigSpec.BooleanValue
    lateinit var PLAYER_POS_X: ForgeConfigSpec.IntValue
    lateinit var PLAYER_POS_Y: ForgeConfigSpec.IntValue
    lateinit var PLAYER_SCALE: ForgeConfigSpec.DoubleValue
    lateinit var PLAYER_YAW_OFFSET: ForgeConfigSpec.DoubleValue

    fun save() = GeneralConfig.save()

    fun define(builder: ForgeConfigSpec.Builder) {
        builder.push("extra_player_render")
        builder.comment("Whether to display player")
        DISABLE_PLAYER_RENDER = builder.define("DisablePlayerRender", false)
        builder.comment("Player position x in screen")
        PLAYER_POS_X = builder.defineInRange("PlayerPosX", 10, 0, Int.MAX_VALUE)
        builder.comment("Player position y in screen")
        PLAYER_POS_Y = builder.defineInRange("PlayerPosY", 10, 0, Int.MAX_VALUE)
        builder.comment("Player scale in screen")
        PLAYER_SCALE = builder.defineInRange("PlayerScale", 40.0, 8.0, 360.0)
        builder.comment("Player yaw offset in screen")
        PLAYER_YAW_OFFSET = builder.defineInRange("PlayerYawOffset", 5.0, Double.MIN_VALUE, Double.MAX_VALUE)
        builder.pop()
    }
}
