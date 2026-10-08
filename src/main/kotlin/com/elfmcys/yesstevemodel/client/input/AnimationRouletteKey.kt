package com.elfmcys.yesstevemodel.client.input

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.client.gui.AnimationRouletteScreen
import com.elfmcys.yesstevemodel.config.GeneralConfig
import com.elfmcys.yesstevemodel.config.ServerConfig
import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.util.InputUtil
import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import rip.ysm.api.client.KeyMappingFactory
import rip.ysm.api.client.event.ClientRawInputEvent
import rip.ysm.api.event.EventResult
import rip.ysm.compat.touhoulittlemaid.TouhouLittleMaidCompat
import rip.ysm.gui.ModernAnimationRouletteScreen

@Environment(EnvType.CLIENT)
object AnimationRouletteKey {
    @JvmField
    val KEY_ROULETTE: KeyMapping = KeyMappingFactory.createInGameNone(
        "key.yes_steve_model.animation_roulette.desc",
        InputConstants.Type.KEYSYM,
        90,
        KeyMappingFactory.YSM_CATEGORY
    )

    @JvmField
    val KEY_LOCK: KeyMapping = KeyMappingFactory.createInGameAlt(
        "key.yes_steve_model.lock_roulette.desc",
        InputConstants.Type.KEYSYM,
        76,
        KeyMappingFactory.YSM_CATEGORY
    )

    init {
        ClientRawInputEvent.KEY_PRESSED.register { _, action, event ->
            if (YesSteveModel.isAvailable() && InputUtil.isPlayerReady() && action == 1 && InputUtil.isKeyPressed(
                    event,
                    KEY_ROULETTE
                )
            ) {
                if (!NetworkHandler.isClientConnected() || ServerConfig.CAN_SWITCH_MODEL.get()) {
                    if (TouhouLittleMaidCompat.isMaidChatAvailable()) {
                        TouhouLittleMaidCompat.openMaidChat()
                    } else {
                        val player = Minecraft.getInstance().player
                        if (player != null) {
                            PlayerCapability[player]?.let { cap ->
                                val modelId = cap.modelId
                                val modelAssembly = cap.getModelAssembly()
                                if (modelAssembly != null && modelAssembly.modelData.modelProperties.extraAnimation.isNotEmpty()) {
                                    val currentScreen = Minecraft.getInstance().screen
                                    when (currentScreen) {
                                        null -> {
                                            when {
                                                GeneralConfig.effectiveModernRoulette() -> {
                                                    Minecraft.getInstance()
                                                        .setScreen(
                                                            ModernAnimationRouletteScreen(
                                                                modelId,
                                                                modelAssembly,
                                                                cap
                                                            )
                                                        )
                                                }

                                                else -> {
                                                    Minecraft.getInstance()
                                                        .setScreen(AnimationRouletteScreen(modelId, modelAssembly, cap))
                                                }
                                            }
                                        }

                                        is AnimationRouletteScreen, is ModernAnimationRouletteScreen -> {
                                            Minecraft.getInstance().setScreen(null)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            EventResult.pass()
        }
    }
}