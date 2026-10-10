package rip.ysm.compat.firstperson

import rip.ysm.compat.ModCompat
import rip.ysm.compat.firstperson.fabric.FirstPersonCompatImpl

object FirstPersonCompat : ModCompat("firstperson") {
    fun isFirstPersonActive(): Boolean = isModLoaded && FirstPersonCompatImpl.isFirstPersonActive()

    fun shouldHideHead(): Boolean = isModLoaded && FirstPersonCompatImpl.shouldHideHead()

    fun setCameraDistance(distance: Float) {
        if (!isModLoaded) return
        FirstPersonCompatImpl.setCameraDistance(distance)
    }
}
