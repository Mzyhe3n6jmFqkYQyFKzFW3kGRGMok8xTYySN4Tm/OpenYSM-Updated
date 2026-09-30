package rip.ysm.compat.realcamera

import rip.ysm.compat.realcamera.fabric.RealCameraCompatImpl

object RealCameraCompat {
    @JvmStatic
    fun isLoaded(): Boolean = RealCameraCompatImpl.isLoaded()

    @JvmStatic
    fun isActive(): Boolean = RealCameraCompatImpl.isActive()
}
