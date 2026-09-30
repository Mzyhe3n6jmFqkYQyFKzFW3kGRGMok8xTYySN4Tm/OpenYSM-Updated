package rip.ysm.compat.firstperson.fabric

import net.fabricmc.loader.api.FabricLoader

object FirstPersonCompatImpl {
    @JvmStatic
    fun isLoaded(): Boolean = FabricLoader.getInstance().isModLoaded("firstperson")

    @JvmStatic
    fun isFirstPersonActive(): Boolean = false

    @JvmStatic
    fun shouldHideHead(): Boolean = false

    @JvmStatic
    fun setCameraDistance(distance: Float) {
    }
}
