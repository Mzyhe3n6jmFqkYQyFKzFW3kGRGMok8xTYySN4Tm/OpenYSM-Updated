package rip.ysm.compat.realcamera.fabric

import rip.ysm.compat.ModCompat

object RealCameraCompatImpl : ModCompat("realcamera") {
    fun isActive(): Boolean = false
}
