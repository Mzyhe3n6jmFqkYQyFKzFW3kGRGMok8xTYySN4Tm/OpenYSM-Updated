package com.elfmcys.yesstevemodel.fabric.client

import com.elfmcys.yesstevemodel.client.ClientModelManager
import com.elfmcys.yesstevemodel.client.renderer.AnimationDebugOverlay
import com.elfmcys.yesstevemodel.client.renderer.ExtraPlayerOverlay
import com.elfmcys.yesstevemodel.client.renderer.ModelSyncStateOverlay
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.Minecraft
import rip.ysm.compat.touhoulittlemaid.fabric.TouhouLittleMaidCompatImpl

class YesSteveModelFabricClient : ClientModInitializer {
    override fun onInitializeClient() {
        val debugOverlay = AnimationDebugOverlay.createOverlay()
        val loadingOverlay = ExtraPlayerOverlay()
        val syncOverlay = ModelSyncStateOverlay()
        // TODO: 'interface HudRenderCallback : Any' is deprecated. Deprecated in Java
        HudRenderCallback.EVENT.register { guiGraphics, tickDelta ->
            val mc = Minecraft.getInstance()
            val w = mc.window.guiScaledWidth
            val h = mc.window.guiScaledHeight
            val partial = tickDelta.getGameTimeDeltaPartialTick(false)
            debugOverlay.render(guiGraphics, mc.font, partial, w, h)
            loadingOverlay.render(guiGraphics, mc.font, partial, w, h)
            syncOverlay.render(guiGraphics, mc.font, partial, w, h)
        }

        // TLM 兼容的客户端装配。必须在此阶段完成：TLM 的 EntityMaidRenderer 构造时读静态钩子，
        // 而渲染器由 EntityRenderDispatcher 在启动后期构造——晚于此处赋值即静默失效。
        TouhouLittleMaidCompatImpl.initClient()

        ClientModelManager.loadDefaultModel()
    }
}
