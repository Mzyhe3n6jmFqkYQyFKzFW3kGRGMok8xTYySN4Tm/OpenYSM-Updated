package com.elfmcys.yesstevemodel.client.renderer

import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.state.CameraRenderState

object RenderContext {
    val COLLECTOR: ThreadLocal<SubmitNodeCollector?> = ThreadLocal()

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

    fun collector(): SubmitNodeCollector? = COLLECTOR.get()

    fun camera(): CameraRenderState? = CAMERA.get()
}