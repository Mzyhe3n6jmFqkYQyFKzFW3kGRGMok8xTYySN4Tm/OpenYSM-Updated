package com.elfmcys.yesstevemodel.client.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import rip.ysm.api.PlatformAPI;

public final class ClientPlayerJoinNotification {

    private static boolean notified = false;

    private ClientPlayerJoinNotification() {
    }

    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> onPlayerJoin(client.player));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> onPlayerQuit(client.player));
    }

    private static void onPlayerJoin(LocalPlayer player) {
        if (notified) {
            return;
        }
        ClientModelManager.runPendingModelCallback();
        notified = true;
        if (!YesSteveModel.isAvailable()) {
            YesSteveModel.sendUnavailableMessage();
            return;
        }
        if (Minecraft.getInstance().isLocalServer()) {
            return;
        }
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(60000L);
                Minecraft.getInstance().execute(() -> {
                    LocalPlayer localPlayer = Minecraft.getInstance().player;
                    if (localPlayer != null && localPlayer.connection.isAcceptingMessages() && !NetworkHandler.isConnectionValid(localPlayer.connection.getConnection())) {
                        localPlayer.displayClientMessage(Component.translatable("message.yes_steve_model.client.server_not_found"), false);
                    }
                });
            } catch (InterruptedException ignored) {
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private static void onPlayerQuit(LocalPlayer player) {
        if (notified) {
            notified = false;
            if (!YesSteveModel.isAvailable()) {
                return;
            }
            ClientModelManager.resetSync();
        }
    }
}
