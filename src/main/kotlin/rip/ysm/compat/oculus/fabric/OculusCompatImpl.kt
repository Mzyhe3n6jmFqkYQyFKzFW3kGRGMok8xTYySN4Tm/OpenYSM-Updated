package rip.ysm.compat.oculus.fabric

import net.fabricmc.loader.api.FabricLoader
import net.irisshaders.iris.api.v0.IrisApi

object OculusCompatImpl {
    private val IRIS_LOADED = FabricLoader.getInstance().isModLoaded("iris")

    @JvmStatic
    fun isLoaded(): Boolean = IRIS_LOADED

    @JvmStatic
    fun isPBRActive(): Boolean = IRIS_LOADED && IrisHolder.shadowPass()

    @JvmStatic
    fun updatePBRState() {
    }

    @JvmStatic
    fun isShaderPackInUse(): Boolean = IRIS_LOADED && IrisHolder.shaderPackInUse()

    @JvmStatic
    fun isRenderingShadowPass(): Boolean = IRIS_LOADED && IrisHolder.shadowPass()

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
