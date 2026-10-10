package rip.ysm.compat.oculus.fabric

import net.irisshaders.iris.api.v0.IrisApi
import rip.ysm.compat.ModCompat

object OculusCompatImpl : ModCompat("iris") {
    fun isPBRActive(): Boolean = runCatching { IrisApi.getInstance().isRenderingShadowPass }.getOrElse { false }

    fun updatePBRState() {
    }

    fun isShaderPackInUse(): Boolean = runCatching { IrisApi.getInstance().isShaderPackInUse }.getOrElse { false }

    fun isRenderingShadowPass(): Boolean =
        runCatching { IrisApi.getInstance().isRenderingShadowPass }.getOrElse { false }
}
