package rip.ysm.compat.oculus.fabric

import net.irisshaders.iris.api.v0.IrisApi

object OculusCompatImpl {
    @JvmStatic
    fun isPBRActive(): Boolean = runCatching { IrisApi.getInstance().isRenderingShadowPass }.getOrElse { false }

    @JvmStatic
    fun updatePBRState() {
    }

    @JvmStatic
    fun isShaderPackInUse(): Boolean = runCatching { IrisApi.getInstance().isShaderPackInUse }.getOrElse { false }

    @JvmStatic
    fun isRenderingShadowPass(): Boolean =
        runCatching { IrisApi.getInstance().isRenderingShadowPass }.getOrElse { false }
}
