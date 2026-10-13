package com.elfmcys.yesstevemodel.event

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.client.ClientOnlySelection
import com.elfmcys.yesstevemodel.client.event.*
import com.elfmcys.yesstevemodel.client.input.*
import com.elfmcys.yesstevemodel.client.renderer.RendererManager
import com.elfmcys.yesstevemodel.model.ServerModelSelection
import rip.ysm.api.PlatformAPI

object YsmEventBootstrap {
    private fun init() {
        // ServerModelSelection registers its auto-save on SERVER_STARTING, so it must be
        // initialised here rather than lazily on the first player join (which happens after
        // SERVER_STARTING has already fired, leaving the listener unregistered).
        Constants.doNothing(
            ServerStartupEvent,
            EnterServerEvent,
            PlayerLogoutEvent,
            CommonEvent,
            CommandRegistry,
            CapabilityEvent,
            ServerModelSelection
        )

        if (PlatformAPI.isServer) return

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
            InputStateKey,
            ClientOnlySelection
        )
    }

    init {
        init()
    }
}
