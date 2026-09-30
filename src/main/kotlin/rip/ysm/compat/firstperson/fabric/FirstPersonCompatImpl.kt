package rip.ysm.compat.firstperson.fabric

import rip.ysm.compat.firstperson.FirstPersonCompat

object FirstPersonCompatImpl {
    @JvmStatic
    fun isLoaded(): Boolean = FirstPersonCompat.isModLoaded

    @JvmStatic
    fun isFirstPersonActive(): Boolean = false

    @JvmStatic
    fun shouldHideHead(): Boolean = false

    @JvmStatic
    fun setCameraDistance(distance: Float) {
    }
}
