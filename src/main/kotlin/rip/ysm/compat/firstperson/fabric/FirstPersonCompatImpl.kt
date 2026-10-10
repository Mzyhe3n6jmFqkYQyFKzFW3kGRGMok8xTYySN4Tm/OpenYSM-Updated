package rip.ysm.compat.firstperson.fabric

import rip.ysm.compat.ModCompat

object FirstPersonCompatImpl : ModCompat("firstperson") {
    fun isFirstPersonActive(): Boolean = false

    fun shouldHideHead(): Boolean = false

    fun setCameraDistance(distance: Float) {
    }
}
