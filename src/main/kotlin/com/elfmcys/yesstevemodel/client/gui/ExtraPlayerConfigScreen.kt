package com.elfmcys.yesstevemodel.client.gui

import com.elfmcys.yesstevemodel.config.ExtraPlayerRenderConfig
import com.elfmcys.yesstevemodel.config.GeneralConfig
import com.elfmcys.yesstevemodel.config.LoadingStateConfig
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import rip.ysm.gui.Option
import rip.ysm.gui.OptionGroup
import rip.ysm.gui.OptionScreen
import rip.ysm.gui.components.BooleanOptionRow
import rip.ysm.gui.components.EnumOptionRow
import rip.ysm.gui.components.SliderOptionRow

class ExtraPlayerConfigScreen(modelScreen: PlayerModelScreen? = null) :
    OptionScreen(Component.literal("OpenYSM"), modelScreen) {

    override fun registerGroups() {
        val general = OptionGroup("general")
            .add(
                SliderOptionRow(
                    0,
                    0,
                    0,
                    22,
                    Option.ofDouble("sound_volume", GeneralConfig.SOUND_VOLUME),
                    0.0,
                    100.0,
                    1.0,
                    "%"
                )
            )
            .add(
                BooleanOptionRow(
                    0,
                    0,
                    0,
                    22,
                    Option.ofBoolean("disable_self_model", GeneralConfig.DISABLE_SELF_MODEL)
                )
            )
            .add(
                BooleanOptionRow(
                    0,
                    0,
                    0,
                    22,
                    Option.ofBoolean("disable_other_model", GeneralConfig.DISABLE_OTHER_MODEL)
                )
            )
            .add(
                BooleanOptionRow(
                    0,
                    0,
                    0,
                    22,
                    Option.ofBoolean("disable_self_hands", GeneralConfig.DISABLE_SELF_HANDS)
                )
            )
            .add(
                BooleanOptionRow(
                    0,
                    0,
                    0,
                    22,
                    Option.ofBoolean("force_client_mode", GeneralConfig.FORCE_CLIENT_MODE)
                )
            )
            .add(
                SliderOptionRow(
                    0,
                    0,
                    0,
                    22,
                    Option.ofDouble("handshake_timeout", GeneralConfig.HANDSHAKE_TIMEOUT),
                    1.0,
                    60.0,
                    0.5,
                    "s"
                )
            )

        val rendering = OptionGroup("rendering")
            .add(
                BooleanOptionRow(
                    0,
                    0,
                    0,
                    22,
                    Option.ofBoolean("disable_player_render", ExtraPlayerRenderConfig.DISABLE_PLAYER_RENDER)
                )
            )
            .add(
                BooleanOptionRow(
                    0,
                    0,
                    0,
                    22,
                    Option.ofBoolean("disable_projectile_model", GeneralConfig.DISABLE_PROJECTILE_MODEL)
                )
            )
            .add(
                BooleanOptionRow(
                    0,
                    0,
                    0,
                    22,
                    Option.ofBoolean("disable_vehicle_model", GeneralConfig.DISABLE_VEHICLE_MODEL)
                )
            )
            .add(
                BooleanOptionRow(
                    0,
                    0,
                    0,
                    22,
                    Option.ofBoolean("disable_external_first_person_anim", GeneralConfig.DISABLE_EXTERNAL_FP_ANIM)
                )
            )

        val performance = OptionGroup("performance")
            .add(
                BooleanOptionRow(
                    0,
                    0,
                    0,
                    22,
                    Option.ofBoolean("use_compatibility_renderer", GeneralConfig.USE_COMPATIBILITY_RENDERER)
                )
            )
            .add(BooleanOptionRow(0, 0, 0, 22, Option.ofBoolean("use_gpu_renderer", GeneralConfig.USE_GPU_RENDERER)))

        val misc = OptionGroup("misc")
            .add(
                BooleanOptionRow(
                    0,
                    0,
                    0,
                    22,
                    Option.ofBoolean("print_animation_roulette_msg", GeneralConfig.PRINT_ANIMATION_ROULETTE_MSG)
                )
            )
            .add(
                BooleanOptionRow(
                    0,
                    0,
                    0,
                    22,
                    Option.ofBoolean("disable_loading_state_screen", LoadingStateConfig.DISABLE_LOADING_STATE_SCREEN)
                )
            )
            .add(
                EnumOptionRow(
                    0,
                    0,
                    0,
                    22,
                    Option.ofEnum("loading_state_position", LoadingStateConfig.LOADING_STATE_POSITION),
                    LoadingStateConfig.Position.entries.toTypedArray()
                )
            )
            .add(
                EnumOptionRow(
                    0,
                    0,
                    0,
                    22,
                    Option.ofEnum("roulette_settings_mode", GeneralConfig.ROULETTE_SETTINGS_MODE),
                    GeneralConfig.RouletteSettingsMode.entries.toTypedArray()
                )
            )
            .add(
                EnumOptionRow(
                    0,
                    0,
                    0,
                    22,
                    Option.ofEnum("roulette_mode", GeneralConfig.ROULETTE_MODE),
                    GeneralConfig.RouletteMode.entries.toTypedArray()
                )
            )
            .add(
                EnumOptionRow(
                    0,
                    0,
                    0,
                    22,
                    Option.ofEnum("texture_screen_mode", GeneralConfig.TEXTURE_SCREEN_MODE),
                    GeneralConfig.TextureScreenMode.entries.toTypedArray()
                )
            )
            .add(
                EnumOptionRow(
                    0,
                    0,
                    0,
                    22,
                    Option.ofEnum("model_info_screen_mode", GeneralConfig.MODEL_INFO_SCREEN_MODE),
                    GeneralConfig.ModelInfoScreenMode.entries.toTypedArray()
                )
            )
            .add(BooleanOptionRow(0, 0, 0, 22, Option.ofBoolean("blur_gui", GeneralConfig.BLUR_GUI)))

        groups.add(general)
        groups.add(rendering)
        groups.add(performance)
        groups.add(misc)
    }

    override fun renderBlurredBackground(guiGraphics: GuiGraphics) {
    }
}