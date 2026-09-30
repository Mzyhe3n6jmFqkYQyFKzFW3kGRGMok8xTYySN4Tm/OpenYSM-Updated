package rip.ysm.compat.optifine

import rip.ysm.compat.ModCompat
import rip.ysm.compat.optifine.fabric.OptiFineDetectorImpl

object OptiFineDetector : ModCompat("optifabric") {
    @JvmStatic
    fun isLoaded(): Boolean = isModLoaded

    @JvmStatic
    fun isOptifinePresent(): Boolean = OptiFineDetectorImpl.isOptifinePresent()
}
