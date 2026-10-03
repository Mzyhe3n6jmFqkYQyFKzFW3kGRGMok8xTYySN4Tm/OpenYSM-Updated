package com.elfmcys.yesstevemodel.util.log

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

object ChatLogger : ILogger {
    override fun logFormatted(str: String, vararg objArr: Any) {
        logComponent(Component.literal(String.format(str, *objArr)))
    }

    override fun logComponent(component: Component) {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        mc.execute {
            player.displayClientMessage(
                Component.translatable("message.yes_steve_model.model.debug_animation.output").append(component),
                false
            )
        }
    }
}