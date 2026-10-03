package com.elfmcys.yesstevemodel.client.input

import com.elfmcys.yesstevemodel.YesSteveModel
import com.elfmcys.yesstevemodel.capability.PlayerCapability
import com.elfmcys.yesstevemodel.client.event.AnimationLockEvent
import com.elfmcys.yesstevemodel.client.gui.AnimationRouletteScreen
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool
import com.elfmcys.yesstevemodel.network.NetworkHandler
import com.elfmcys.yesstevemodel.network.message.C2SPlayAnimationPacket
import com.elfmcys.yesstevemodel.util.InputUtil
import com.google.common.collect.Lists
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.input.KeyEvent
import rip.ysm.api.PlatformAPI
import rip.ysm.api.client.KeyMappingFactory
import rip.ysm.api.client.event.ClientRawInputEvent
import rip.ysm.api.event.EventResult

object ExtraAnimationKey {
    @JvmField
    val KEY_MAPPINGS: MutableList<KeyMapping> = Lists.newArrayList()

    @Volatile
    private var initialized: Boolean = false

    @JvmStatic
    fun getKeyMappings(): MutableList<KeyMapping> {
        if (!initialized) {
            initialized = true
            if (YesSteveModel.isAvailable()) {
                for (i in 0..7) {
                    val eventMapping = KeyMappingFactory.createInGameNone(
                        "key.yes_steve_model.extra_animation.$i.desc",
                        InputConstants.Type.KEYSYM,
                        -1,
                        KeyMappingFactory.YSM_CATEGORY
                    )
                    KEY_MAPPINGS.add(eventMapping)
                }
            }
        }
        return KEY_MAPPINGS
    }

    @JvmStatic
    fun register() {
        if (PlatformAPI.isServer()) {
            return
        }
        ClientRawInputEvent.KEY_PRESSED.register { _, action, event ->
            onKeyInput(action, event)
            EventResult.pass()
        }
    }

    @JvmStatic
    private fun onKeyInput(action: Int, event: KeyEvent) {
        if (!YesSteveModel.isAvailable() || !InputUtil.isPlayerReady()) {
            return
        }
        val localPlayer = Minecraft.getInstance().player ?: return
        for (eventMapping in KEY_MAPPINGS) {
            if (action == 1 && InputUtil.isKeyPressed(event, eventMapping) && !AnimationLockEvent.isPlayerMoving(
                    localPlayer
                )
            ) {
                PlayerCapability[localPlayer]?.let { cap ->
                    val modelAssembly = cap.getModelAssembly() ?: return@let
                    val index = KEY_MAPPINGS.indexOf(eventMapping)
                    val modelProperties = modelAssembly.modelData.modelProperties
                    val map = modelProperties.extraAnimation
                    if (map.size > index) {
                        val rouletteKey = map.getKeyAt(index)
                        if (rouletteKey == "#return") {
                            NetworkHandler.sendToServer(C2SPlayAnimationPacket.createDefault())
                            return@let
                        }
                        if (rouletteKey.startsWith("#") && modelProperties.extraAnimationClassify.containsKey(
                                rouletteKey.substring(1)
                            )
                        ) {
                            AnimationRouletteScreen.setInitialSubmenu(rouletteKey.substring(1))
                            Minecraft.getInstance().setScreen(
                                AnimationRouletteScreen(
                                    modelProperties.extraAnimationButtons.toMutableMap(),
                                    modelProperties.extraAnimationClassify.toMutableMap(),
                                    modelAssembly,
                                    cap
                                )
                            )
                            return@let
                        }
                        NetworkHandler.sendToServer(C2SPlayAnimationPacket(index, StringPool.EMPTY))
                    }
                }
                return
            }
        }
    }
}