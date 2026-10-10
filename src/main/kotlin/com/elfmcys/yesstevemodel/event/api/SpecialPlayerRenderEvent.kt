package com.elfmcys.yesstevemodel.event.api

import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Player
import rip.ysm.api.event.EventResult

class SpecialPlayerRenderEvent(
    val player: Player? = null,
    val customPlayer: CustomPlayerEntity? = null,
    val modelId: String? = null
) {
    var textureLocation: Identifier? = null

    fun interface RenderHandler {
        fun onRender(event: SpecialPlayerRenderEvent): EventResult
    }

    companion object {
        val EVENT: Event<RenderHandler> = EventFactory.createArrayBacked(RenderHandler::class.java) { listeners ->
            RenderHandler { event ->
                for (listener in listeners) {
                    val result = listener.onRender(event)
                    if (result.interrupts()) {
                        return@RenderHandler result
                    }
                }
                EventResult.pass()
            }
        }

        fun post(event: SpecialPlayerRenderEvent): EventResult {
            return EVENT.invoker().onRender(event)
        }
    }
}
