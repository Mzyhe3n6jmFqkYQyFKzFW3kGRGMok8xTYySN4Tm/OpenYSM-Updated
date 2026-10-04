package rip.ysm.compat.firstperson.fabric

import rip.ysm.compat.ModCompat

object FirstPersonCompatImpl : ModCompat("firstperson") {
    @JvmStatic
    fun isFirstPersonActive(): Boolean = false

    @JvmStatic
    fun shouldHideHead(): Boolean = false

    @JvmStatic
    fun setCameraDistance(distance: Float) {
    }
}
