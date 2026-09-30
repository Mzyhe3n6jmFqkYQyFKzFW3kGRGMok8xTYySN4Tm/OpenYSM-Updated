package com.elfmcys.yesstevemodel.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

public final class PlayerLogoutEvent {

    private PlayerLogoutEvent() {
    }

    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (!YesSteveModel.isAvailable()) {
                return;
            }
            ServerPlayer player = handler.player;
            if (player != null && NetworkHandler.isPlayerConnected(player)) {
                ServerModelManager.syncModelToPlayer(player.getUUID());
            }
        });
    }
}
