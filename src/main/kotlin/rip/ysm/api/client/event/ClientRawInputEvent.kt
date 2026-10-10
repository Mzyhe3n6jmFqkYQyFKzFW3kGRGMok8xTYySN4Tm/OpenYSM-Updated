package rip.ysm.api.client.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.client.Minecraft
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonInfo
import rip.ysm.api.event.EventResult

object ClientRawInputEvent {
    fun interface KeyPressed {
        fun onKey(client: Minecraft, action: Int, event: KeyEvent): EventResult
    }

    fun interface MouseClickedPre {
        fun onMouseClick(client: Minecraft, buttonInfo: MouseButtonInfo, action: Int): EventResult
    }

    @JvmField
    val KEY_PRESSED: Event<KeyPressed> = EventFactory.createArrayBacked(KeyPressed::class.java) { listeners ->
        KeyPressed { client, action, event ->
            for (listener in listeners) {
                val result = listener.onKey(client, action, event)
                if (result.interrupts()) {
                    return@KeyPressed result
                }
            }
            EventResult.pass()
        }
    }

    @JvmField
    val MOUSE_CLICKED_PRE: Event<MouseClickedPre> =
        EventFactory.createArrayBacked(MouseClickedPre::class.java) { listeners ->
            MouseClickedPre { client, buttonInfo, action ->
                for (listener in listeners) {
                    val result = listener.onMouseClick(client, buttonInfo, action)
                    if (result.interrupts()) {
                        return@MouseClickedPre result
                    }
                }
                EventResult.pass()
            }
        }
}