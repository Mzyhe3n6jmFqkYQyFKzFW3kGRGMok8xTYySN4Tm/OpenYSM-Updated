package rip.ysm.compat.oculus

import rip.ysm.compat.ModCompat
import rip.ysm.compat.oculus.fabric.OculusCompatImpl

object OculusCompat : ModCompat("iris") {
    fun isPBRActive(): Boolean {
        return isModLoaded && OculusCompatImpl.isPBRActive()
    }

    fun updatePBRState() {
        if (!isModLoaded) return
        OculusCompatImpl.updatePBRState()
    }

    fun isShaderPackInUse(): Boolean = isModLoaded && OculusCompatImpl.isShaderPackInUse()

    fun isRenderingShadowPass(): Boolean = isModLoaded && OculusCompatImpl.isRenderingShadowPass()
}
