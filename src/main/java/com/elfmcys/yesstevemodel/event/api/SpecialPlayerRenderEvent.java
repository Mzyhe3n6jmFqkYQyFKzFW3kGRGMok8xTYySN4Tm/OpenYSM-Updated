package com.elfmcys.yesstevemodel.event.api;

import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import rip.ysm.api.event.EventResult;

public class SpecialPlayerRenderEvent {

    public static final Event<RenderHandler> EVENT = EventFactory.createArrayBacked(RenderHandler.class, listeners -> event -> {
        for (RenderHandler listener : listeners) {
            EventResult result = listener.onRender(event);
            if (result != null && result.interrupts()) {
                return result;
            }
        }
        return EventResult.pass();
    });

    @FunctionalInterface
    public interface RenderHandler {
        EventResult onRender(SpecialPlayerRenderEvent event);
    }

    public static EventResult post(SpecialPlayerRenderEvent event) {
        return EVENT.invoker().onRender(event);
    }

    private final Player player;

    private final CustomPlayerEntity customPlayer;

    private final String modelId;

    @Nullable
    private Identifier textureLocation;

    public SpecialPlayerRenderEvent() {
        this.player = null;
        this.customPlayer = null;
        this.modelId = null;
    }

    public SpecialPlayerRenderEvent(Player player, CustomPlayerEntity customPlayer, String str) {
        this.player = player;
        this.customPlayer = customPlayer;
        this.modelId = str;
    }

    public Player getPlayer() {
        return this.player;
    }

    public CustomPlayerEntity getCustomPlayer() {
        return this.customPlayer;
    }

    public String getModelId() {
        return this.modelId;
    }

    @Nullable
    public Identifier getTextureLocation() {
        return this.textureLocation;
    }

    public void setTextureLocation(@Nullable Identifier identifier) {
        this.textureLocation = identifier;
    }
}
