package rip.ysm.compat.realcamera.fabric

import rip.ysm.compat.realcamera.RealCameraCompat

object RealCameraCompatImpl {
    @JvmStatic
    fun isLoaded(): Boolean = RealCameraCompat.isModLoaded

    @JvmStatic
    fun isActive(): Boolean = false
}
