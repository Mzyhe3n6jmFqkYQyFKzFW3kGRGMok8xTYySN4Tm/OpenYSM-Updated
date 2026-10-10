package rip.ysm.compat.optifine.fabric

import rip.ysm.compat.ModCompat

object OptiFineDetectorImpl : ModCompat("optifabric") {
    fun isOptifinePresent(): Boolean = false
}
