package rip.ysm.api.client.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
import rip.ysm.api.event.EventResult;

public final class ClientRawInputEvent {

    public static final Event<KeyPressed> KEY_PRESSED = EventFactory.createArrayBacked(KeyPressed.class, listeners -> (client, action, event) -> {
        for (KeyPressed listener : listeners) {
            EventResult result = listener.onKey(client, action, event);
            if (result != null && result.interrupts()) {
                return result;
            }
        }
        return EventResult.pass();
    });

    public static final Event<MouseClickedPre> MOUSE_CLICKED_PRE = EventFactory.createArrayBacked(MouseClickedPre.class, listeners -> (client, buttonInfo, action) -> {
        for (MouseClickedPre listener : listeners) {
            EventResult result = listener.onMouseClick(client, buttonInfo, action);
            if (result != null && result.interrupts()) {
                return result;
            }
        }
        return EventResult.pass();
    });

    @FunctionalInterface
    public interface KeyPressed {
        EventResult onKey(Minecraft client, int action, KeyEvent event);
    }

    @FunctionalInterface
    public interface MouseClickedPre {
        EventResult onMouseClick(Minecraft client, MouseButtonInfo buttonInfo, int action);
    }
}
