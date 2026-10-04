package rip.ysm.compat.realcamera.fabric

import rip.ysm.compat.ModCompat

object RealCameraCompatImpl : ModCompat("realcamera") {
    @JvmStatic
    fun isActive(): Boolean = false
}
