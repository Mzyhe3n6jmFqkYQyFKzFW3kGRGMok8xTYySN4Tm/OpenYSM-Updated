package rip.ysm.compat.realcamera

import rip.ysm.compat.ModCompat
import rip.ysm.compat.realcamera.fabric.RealCameraCompatImpl

object RealCameraCompat : ModCompat("realcamera") {
    fun isActive(): Boolean {
        return isModLoaded && RealCameraCompatImpl.isActive()
    }
}
