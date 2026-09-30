package rip.ysm.compat.realcamera.fabric

import net.fabricmc.loader.api.FabricLoader

object RealCameraCompatImpl {
    @JvmStatic
    fun isLoaded(): Boolean = FabricLoader.getInstance().isModLoaded("realcamera")

    @JvmStatic
    fun isActive(): Boolean = false
}
