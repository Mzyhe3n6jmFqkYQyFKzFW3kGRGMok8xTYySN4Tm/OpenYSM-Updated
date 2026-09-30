package rip.ysm.compat.firstperson

import rip.ysm.compat.ModCompat
import rip.ysm.compat.firstperson.fabric.FirstPersonCompatImpl

object FirstPersonCompat : ModCompat("firstperson") {
    @JvmStatic
    fun isLoaded(): Boolean = isModLoaded

    @JvmStatic
    fun isFirstPersonActive(): Boolean = FirstPersonCompatImpl.isFirstPersonActive()

    @JvmStatic
    fun shouldHideHead(): Boolean = FirstPersonCompatImpl.shouldHideHead()

    @JvmStatic
    fun setCameraDistance(distance: Float) {
        FirstPersonCompatImpl.setCameraDistance(distance)
    }
}
