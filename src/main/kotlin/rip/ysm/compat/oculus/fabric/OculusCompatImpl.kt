package rip.ysm.compat.oculus.fabric

import net.irisshaders.iris.api.v0.IrisApi
import rip.ysm.compat.oculus.OculusCompat

object OculusCompatImpl {
    @JvmStatic
    fun isLoaded(): Boolean = OculusCompat.isModLoaded

    @JvmStatic
    fun isPBRActive(): Boolean = OculusCompat.isModLoaded && IrisHolder.shadowPass()

    @JvmStatic
    fun updatePBRState() {
    }

    @JvmStatic
    fun isShaderPackInUse(): Boolean = OculusCompat.isModLoaded && IrisHolder.shaderPackInUse()

    @JvmStatic
    fun isRenderingShadowPass(): Boolean = OculusCompat.isModLoaded && IrisHolder.shadowPass()

    private object IrisHolder {
        @JvmStatic
        fun shaderPackInUse(): Boolean {
            return try {
                IrisApi.getInstance().isShaderPackInUse
            } catch (t: Throwable) {
                false
            }
        }

        @JvmStatic
        fun shadowPass(): Boolean {
            return try {
                IrisApi.getInstance().isRenderingShadowPass
            } catch (t: Throwable) {
                false
            }
        }
    }
}
