package rip.ysm.compat.firstperson.fabric

object FirstPersonCompatImpl {
    @JvmStatic
    fun isFirstPersonActive(): Boolean = false

    @JvmStatic
    fun shouldHideHead(): Boolean = false

    @JvmStatic
    fun setCameraDistance(distance: Float) {
    }
}
