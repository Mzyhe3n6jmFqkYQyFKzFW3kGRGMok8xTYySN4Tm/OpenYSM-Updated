package rip.ysm.compat.optifine.fabric

import rip.ysm.compat.ModCompat

object OptiFineDetectorImpl : ModCompat("optifabric") {
    @JvmStatic
    fun isOptifinePresent(): Boolean = false
}
