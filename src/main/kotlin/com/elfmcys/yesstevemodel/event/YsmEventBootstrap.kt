package com.elfmcys.yesstevemodel.event

import com.elfmcys.yesstevemodel.Constants
import com.elfmcys.yesstevemodel.client.event.*
import com.elfmcys.yesstevemodel.client.input.*
import com.elfmcys.yesstevemodel.client.renderer.RendererManager
import rip.ysm.api.PlatformAPI

// TODO
object YsmEventBootstrap {
    init {
        Constants.doNothing(
            ServerStartupEvent,
            EnterServerEvent,
            PlayerLogoutEvent,
            CommonEvent,
            CommandRegistry,
            CapabilityEvent
        )

        if (!PlatformAPI.isServer()) {
            Constants.doNothing(
                EntityJoinCallbackEvent,
                ClientSetupEvent
            )

            ClientTickEvent.register()
            ClientPlayerJoinNotification.register()
            ClientPlayerCloneEvent.register()
            AnimationLockEvent.register()
            PlayerSkinTextureManager.register()
            RendererManager.register()
            PlayerModelToggleKey.register()
            AnimationRouletteKey.register()
            DebugAnimationKey.register()
            ExtraPlayerRenderKey.register()
            ExtraAnimationKey.register()
            InputStateKey.register()
        }
    }
}
