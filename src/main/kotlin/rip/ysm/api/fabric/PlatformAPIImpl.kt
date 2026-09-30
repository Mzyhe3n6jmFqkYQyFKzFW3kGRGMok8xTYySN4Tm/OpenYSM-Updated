package rip.ysm.api.fabric

import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader

object PlatformAPIImpl {
    @JvmStatic
    fun isServer(): Boolean {
        return FabricLoader.getInstance().environmentType == EnvType.SERVER
    }

    @JvmStatic
    fun getPlatformName(): String {
        return "Fabric"
    }
}
