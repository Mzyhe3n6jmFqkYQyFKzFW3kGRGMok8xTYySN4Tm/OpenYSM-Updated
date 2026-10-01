package rip.ysm.compat.optifine

import rip.ysm.compat.ModCompat
import rip.ysm.compat.optifine.fabric.OptiFineDetectorImpl

object OptiFineDetector : ModCompat("optifabric") {
    @JvmStatic
    fun isOptifinePresent(): Boolean = isModLoaded && OptiFineDetectorImpl.isOptifinePresent()
}
