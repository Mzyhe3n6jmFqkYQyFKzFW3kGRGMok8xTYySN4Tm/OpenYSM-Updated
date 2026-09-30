package rip.ysm.compat.oculus

import rip.ysm.compat.oculus.fabric.OculusCompatImpl

object OculusCompat {
    @JvmStatic
    fun isLoaded(): Boolean = OculusCompatImpl.isLoaded()

    @JvmStatic
    fun isPBRActive(): Boolean = OculusCompatImpl.isPBRActive()

    @JvmStatic
    fun updatePBRState() {
        OculusCompatImpl.updatePBRState()
    }

    @JvmStatic
    fun isShaderPackInUse(): Boolean = OculusCompatImpl.isShaderPackInUse()

    @JvmStatic
    fun isRenderingShadowPass(): Boolean = OculusCompatImpl.isRenderingShadowPass()
}
