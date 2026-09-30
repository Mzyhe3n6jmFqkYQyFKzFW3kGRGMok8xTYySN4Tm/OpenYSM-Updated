package rip.ysm.compat.optifine.fabric

import net.fabricmc.loader.api.FabricLoader

object OptiFineDetectorImpl {
    @JvmStatic
    fun isOptifinePresent(): Boolean = FabricLoader.getInstance().isModLoaded("optifabric")
}
