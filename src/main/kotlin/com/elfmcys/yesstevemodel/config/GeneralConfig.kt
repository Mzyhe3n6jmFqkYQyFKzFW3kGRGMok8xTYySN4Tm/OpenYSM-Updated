package com.elfmcys.yesstevemodel.config

import net.minecraftforge.common.ForgeConfigSpec

object GeneralConfig {
    enum class RouletteSettingsMode {
        MODERN,
        CLASSIC
    }

    enum class RouletteMode {
        MODERN,
        CLASSIC
    }

    enum class TextureScreenMode {
        MODERN,
        CLASSIC
    }

    enum class ModelInfoScreenMode {
        MODERN,
        CLASSIC
    }

    lateinit var DISCLAIMER_SHOW: ForgeConfigSpec.BooleanValue
    lateinit var PRINT_ANIMATION_ROULETTE_MSG: ForgeConfigSpec.BooleanValue
    lateinit var DISABLE_SELF_MODEL: ForgeConfigSpec.BooleanValue
    lateinit var DISABLE_OTHER_MODEL: ForgeConfigSpec.BooleanValue
    lateinit var DISABLE_SELF_HANDS: ForgeConfigSpec.BooleanValue
    lateinit var DISABLE_PROJECTILE_MODEL: ForgeConfigSpec.BooleanValue
    lateinit var DISABLE_VEHICLE_MODEL: ForgeConfigSpec.BooleanValue
    lateinit var DISABLE_EXTERNAL_FP_ANIM: ForgeConfigSpec.BooleanValue
    lateinit var USE_COMPATIBILITY_RENDERER: ForgeConfigSpec.BooleanValue
    lateinit var SOUND_VOLUME: ForgeConfigSpec.DoubleValue
    lateinit var SHOW_MODEL_ID_FIRST: ForgeConfigSpec.BooleanValue
    lateinit var SOPHISTICATEDBACKPACK: ForgeConfigSpec.BooleanValue
    lateinit var PARCOOL: ForgeConfigSpec.BooleanValue
    lateinit var USE_GPU_RENDERER: ForgeConfigSpec.BooleanValue
    lateinit var ROULETTE_SETTINGS_MODE: ForgeConfigSpec.EnumValue<RouletteSettingsMode>
    lateinit var ROULETTE_MODE: ForgeConfigSpec.EnumValue<RouletteMode>
    lateinit var BLUR_GUI: ForgeConfigSpec.BooleanValue
    lateinit var TEXTURE_SCREEN_MODE: ForgeConfigSpec.EnumValue<TextureScreenMode>
    lateinit var MODEL_INFO_SCREEN_MODE: ForgeConfigSpec.EnumValue<ModelInfoScreenMode>

    @JvmStatic
    fun effectiveModernRoulette(): Boolean =
        !(!::ROULETTE_MODE.isInitialized || !::ROULETTE_SETTINGS_MODE.isInitialized) && ROULETTE_MODE.get() == RouletteMode.MODERN && ROULETTE_SETTINGS_MODE.get() == RouletteSettingsMode.MODERN

    @JvmStatic
    fun buildSpec(): ForgeConfigSpec {
        val builder = ForgeConfigSpec.Builder()
        defineGeneral(builder)
        ExtraPlayerRenderConfig.define(builder)
        LoadingStateConfig.define(builder)
        return builder.build()
    }

    @JvmStatic
    fun defineGeneral(builder: ForgeConfigSpec.Builder) {
        builder.push("general")
        builder.comment("Whether to display disclaimer GUI")
        DISCLAIMER_SHOW = builder.define("DisclaimerShow", true)
        builder.comment("Whether to print animation roulette play message")
        PRINT_ANIMATION_ROULETTE_MSG = builder.define("PrintAnimationRouletteMsg", false)
        builder.comment("Prevents rendering of self player's model")
        DISABLE_SELF_MODEL = builder.define("DisableSelfModel", false)
        builder.comment("Prevents rendering of other player's model")
        DISABLE_OTHER_MODEL = builder.define("DisableOtherModel", false)
        builder.comment("Prevents rendering of self player's hand")
        DISABLE_SELF_HANDS = builder.define("DisableSelfHands", false)
        builder.comment("Prevents rendering of projectile model")
        DISABLE_PROJECTILE_MODEL = builder.define("DisableProjectileModel", false)
        builder.comment("Prevents rendering of vehicle model")
        DISABLE_VEHICLE_MODEL = builder.define("DisableVehicleModel", false)
        builder.comment("Disable first person animation from other mods.")
        DISABLE_EXTERNAL_FP_ANIM = builder.define("DisableExternalFirstPersonAnim", false)
        builder.comment("If rendering errors occur, try turning on this.")
        USE_COMPATIBILITY_RENDERER = builder.define("UseCompatibilityRenderer", false)
        builder.comment("Test renderer.")
        USE_GPU_RENDERER = builder.define("UseGpuRenderer", true)
        ROULETTE_SETTINGS_MODE = builder.defineEnum("RouletteSettingsMode", RouletteSettingsMode.MODERN)
        ROULETTE_MODE = builder.defineEnum("RouletteMode", RouletteMode.CLASSIC)
        BLUR_GUI = builder.define("BlurGui", true)
        TEXTURE_SCREEN_MODE = builder.defineEnum("TextureScreenMode", TextureScreenMode.MODERN)
        MODEL_INFO_SCREEN_MODE = builder.defineEnum("ModelInfoScreenMode", ModelInfoScreenMode.MODERN)
        builder.comment("The amount of volume when the animation is played.")
        SOUND_VOLUME = builder.defineInRange("SoundVolume", 100.0, 0.0, 100.0)
        builder.comment("Whether to display model ID first in the model selection screen, instead of the model name filled in by the model author.")
        SHOW_MODEL_ID_FIRST = builder.define("ShowModelIdFirst", false)
        builder.pop()
        builder.push("Integration")
        SOPHISTICATEDBACKPACK = builder.define("SophisticatedBackpack", true)
        PARCOOL = builder.define("Parcool", true)
        builder.pop()
    }
}
