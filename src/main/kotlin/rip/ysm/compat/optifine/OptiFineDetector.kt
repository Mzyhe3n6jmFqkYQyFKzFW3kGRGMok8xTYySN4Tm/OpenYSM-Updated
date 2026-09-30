package rip.ysm.compat.optifine

import rip.ysm.compat.optifine.fabric.OptiFineDetectorImpl

object OptiFineDetector {
    @JvmStatic
    fun isOptifinePresent(): Boolean = OptiFineDetectorImpl.isOptifinePresent()
}
