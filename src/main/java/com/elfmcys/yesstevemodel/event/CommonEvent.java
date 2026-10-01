package com.elfmcys.yesstevemodel.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import rip.ysm.compat.touhoulittlemaid.TouhouMaidCompat;

import java.io.IOException;

public final class CommonEvent {

    private CommonEvent() {
    }

    public static Object nativeInit() {
        try {
            ServerModelManager.reloadPacks();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static void register() {
        if (!YesSteveModel.isAvailable()) {
            Constants.LOGGER.error(YesSteveModel.getErrorMessage());
            return;
        }
        NetworkHandler.init();
        TouhouMaidCompat.init();
        nativeInit();
    }
}
