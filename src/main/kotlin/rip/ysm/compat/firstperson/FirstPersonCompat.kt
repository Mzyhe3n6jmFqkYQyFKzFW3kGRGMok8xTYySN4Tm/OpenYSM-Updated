package rip.ysm.compat.firstperson

import rip.ysm.compat.ModCompat
import rip.ysm.compat.firstperson.fabric.FirstPersonCompatImpl

object FirstPersonCompat : ModCompat("firstperson") {
    @JvmStatic
    fun isFirstPersonActive(): Boolean {
        if (!isModLoaded) return false
        return FirstPersonCompatImpl.isFirstPersonActive()
    }

    @JvmStatic
    fun shouldHideHead(): Boolean {
        if (!isModLoaded) return false
        return FirstPersonCompatImpl.shouldHideHead()
    }

    @JvmStatic
    fun setCameraDistance(distance: Float) {
        if (!isModLoaded) return
        FirstPersonCompatImpl.setCameraDistance(distance)
    }
}
