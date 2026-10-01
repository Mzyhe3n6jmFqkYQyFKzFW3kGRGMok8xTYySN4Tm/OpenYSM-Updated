package rip.ysm.compat.firstperson

import rip.ysm.compat.ModCompat
import rip.ysm.compat.firstperson.fabric.FirstPersonCompatImpl

object FirstPersonCompat : ModCompat("firstperson") {
    @JvmStatic
    fun isFirstPersonActive(): Boolean = isModLoaded && FirstPersonCompatImpl.isFirstPersonActive()

    @JvmStatic
    fun shouldHideHead(): Boolean = isModLoaded && FirstPersonCompatImpl.shouldHideHead()

    @JvmStatic
    fun setCameraDistance(distance: Float) {
        if (!isModLoaded) return
        FirstPersonCompatImpl.setCameraDistance(distance)
    }
}
