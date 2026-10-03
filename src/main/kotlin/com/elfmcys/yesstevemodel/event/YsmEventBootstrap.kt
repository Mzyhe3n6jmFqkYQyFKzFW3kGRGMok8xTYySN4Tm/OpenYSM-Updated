package com.elfmcys.yesstevemodel.event

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.client.event.*
import com.elfmcys.yesstevemodel.client.input.*
import com.elfmcys.yesstevemodel.client.renderer.RendererManager
import rip.ysm.api.PlatformAPI

object YsmEventBootstrap {
    private fun init() {
        Constants.doNothing(
            ServerStartupEvent,
            EnterServerEvent,
            PlayerLogoutEvent,
            CommonEvent,
            CommandRegistry,
            CapabilityEvent
        )

        if (PlatformAPI.isServer()) return

        Constants.doNothing(
            EntityJoinCallbackEvent,
            ClientSetupEvent,
            ClientTickEvent,
            ClientPlayerJoinNotification,
            ClientPlayerCloneEvent,
            AnimationLockEvent,
            PlayerSkinTextureManager,
            RendererManager,
            PlayerModelToggleKey,
            AnimationRouletteKey,
            DebugAnimationKey,
            ExtraPlayerRenderKey,
            ExtraAnimationKey,
            InputStateKey
        )
    }

    init {
        init()
    }
}
