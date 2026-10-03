package com.elfmcys.yesstevemodel.client.renderer

import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.state.CameraRenderState

object RenderContext {
    @JvmField
    val COLLECTOR: ThreadLocal<SubmitNodeCollector?> = ThreadLocal()

    @JvmField
    val CAMERA: ThreadLocal<CameraRenderState?> = ThreadLocal()

    @JvmStatic
    fun enter(collector: SubmitNodeCollector?, cameraState: CameraRenderState?) {
        COLLECTOR.set(collector)
        CAMERA.set(cameraState)
    }

    @JvmStatic
    fun exit() {
        COLLECTOR.remove()
        CAMERA.remove()
    }

    @JvmStatic
    fun collector(): SubmitNodeCollector? {
        return COLLECTOR.get()
    }

    @JvmStatic
    fun camera(): CameraRenderState? {
        return CAMERA.get()
    }
}