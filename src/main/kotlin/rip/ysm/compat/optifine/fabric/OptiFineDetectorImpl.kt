package rip.ysm.compat.optifine.fabric

import rip.ysm.compat.optifine.OptiFineDetector

object OptiFineDetectorImpl {
    @JvmStatic
    fun isOptifinePresent(): Boolean = OptiFineDetector.isModLoaded
}
