package rip.ysm.compat.firstperson

import rip.ysm.compat.firstperson.fabric.FirstPersonCompatImpl

object FirstPersonCompat {
    @JvmStatic
    fun isLoaded(): Boolean = FirstPersonCompatImpl.isLoaded()

    @JvmStatic
    fun isFirstPersonActive(): Boolean = FirstPersonCompatImpl.isFirstPersonActive()

    @JvmStatic
    fun shouldHideHead(): Boolean = FirstPersonCompatImpl.shouldHideHead()

    @JvmStatic
    fun setCameraDistance(distance: Float) {
        FirstPersonCompatImpl.setCameraDistance(distance)
    }
}
