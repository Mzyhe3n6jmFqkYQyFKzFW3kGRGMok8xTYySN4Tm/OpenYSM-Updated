package com.elfmcys.yesstevemodel.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class ServerStartupEvent {

    private ServerStartupEvent() {
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            if (!YesSteveModel.isAvailable()) {
                return;
            }
            ServerModelManager.loadModels(result -> {
                if (!result.isSuccess()) {
                    server.execute(() -> {
                        throw new RuntimeException("YSM Loading Failed: " + result.getErrorMessage().getString(256));
                    });
                }
            }, null);
        });
    }
}
