package rip.ysm.compat.oculus

import rip.ysm.compat.ModCompat
import rip.ysm.compat.oculus.fabric.OculusCompatImpl

object OculusCompat : ModCompat("iris") {
    @JvmStatic
    fun isLoaded(): Boolean = isModLoaded

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
