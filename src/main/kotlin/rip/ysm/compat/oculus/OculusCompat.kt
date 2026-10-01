package rip.ysm.compat.oculus

import rip.ysm.compat.ModCompat
import rip.ysm.compat.oculus.fabric.OculusCompatImpl

object OculusCompat : ModCompat("iris") {
    @JvmStatic
    fun isPBRActive(): Boolean {
        return isModLoaded && OculusCompatImpl.isPBRActive()
    }

    @JvmStatic
    fun updatePBRState() {
        if (!isModLoaded) return
        OculusCompatImpl.updatePBRState()
    }

    @JvmStatic
    fun isShaderPackInUse(): Boolean = isModLoaded && OculusCompatImpl.isShaderPackInUse()

    @JvmStatic
    fun isRenderingShadowPass(): Boolean = isModLoaded && OculusCompatImpl.isRenderingShadowPass()
}
